const calendarEl = document.getElementById('calendar');
const filterDate = document.getElementById('filter-date');
const clearFilterBtn = document.getElementById('clear-filter');
const slotForm = document.getElementById('slot-form');
const slotFormError = document.getElementById('slot-form-error');

const bookModal = document.getElementById('book-modal');
const bookForm = document.getElementById('book-form');
const bookSlotLabel = document.getElementById('book-slot-label');
const bookError = document.getElementById('book-error');
const bookCancel = document.getElementById('book-cancel');

let currentBookingSlotId = null;

async function fetchSlots() {
    const date = filterDate.value;
    const url = date ? `/api/slots?date=${encodeURIComponent(date)}` : '/api/slots';
    const res = await fetch(url);
    return res.json();
}

function groupByDate(slots) {
    const groups = new Map();
    for (const slot of slots) {
        if (!groups.has(slot.date)) groups.set(slot.date, []);
        groups.get(slot.date).push(slot);
    }
    return groups;
}

function renderCalendar(slots) {
    calendarEl.innerHTML = '';
    if (slots.length === 0) {
        calendarEl.innerHTML = '<p class="empty-msg">Aucun creneau pour le moment.</p>';
        return;
    }
    const groups = groupByDate(slots);
    const dates = [...groups.keys()].sort();
    for (const date of dates) {
        const dayGroup = document.createElement('div');
        dayGroup.className = 'day-group';

        const heading = document.createElement('h3');
        heading.textContent = formatDate(date);
        dayGroup.appendChild(heading);

        const list = document.createElement('div');
        list.className = 'slot-list';

        for (const slot of groups.get(date)) {
            list.appendChild(renderSlotCard(slot));
        }

        dayGroup.appendChild(list);
        calendarEl.appendChild(dayGroup);
    }
}

function renderSlotCard(slot) {
    const bookings = slot.bookings || [];
    const capacity = slot.capacity || 1;
    const isFull = bookings.length >= capacity;

    const card = document.createElement('div');
    card.className = 'slot-card' + (isFull ? ' full' : '');

    const time = document.createElement('div');
    time.className = 'slot-time';
    time.textContent = slot.time;
    card.appendChild(time);

    const cap = document.createElement('div');
    cap.className = 'slot-capacity';
    cap.textContent = `${bookings.length} / ${capacity} reserve(s)`;
    card.appendChild(cap);

    const actions = document.createElement('div');
    actions.className = 'slot-actions';

    const bookBtn = document.createElement('button');
    bookBtn.type = 'button';
    bookBtn.textContent = isFull ? 'Complet' : 'Reserver';
    bookBtn.disabled = isFull;
    bookBtn.addEventListener('click', () => openBookModal(slot));
    actions.appendChild(bookBtn);

    const deleteBtn = document.createElement('button');
    deleteBtn.type = 'button';
    deleteBtn.textContent = 'Supprimer';
    deleteBtn.className = 'delete-btn';
    deleteBtn.disabled = bookings.length > 0;
    deleteBtn.title = bookings.length > 0 ? 'Impossible : deja reserve' : 'Supprimer ce creneau';
    deleteBtn.addEventListener('click', () => deleteSlot(slot.id));
    actions.appendChild(deleteBtn);

    card.appendChild(actions);
    return card;
}

function formatDate(dateStr) {
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
}

async function refresh() {
    const slots = await fetchSlots();
    renderCalendar(slots);
}

slotForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    slotFormError.textContent = '';
    const date = document.getElementById('new-date').value;
    const time = document.getElementById('new-time').value;
    const capacity = parseInt(document.getElementById('new-capacity').value, 10) || 1;

    const res = await fetch('/api/slots', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ date, time, capacity })
    });
    const data = await res.json();
    if (!res.ok) {
        slotFormError.textContent = data.error || 'Erreur lors de la creation du creneau';
        return;
    }
    slotForm.reset();
    document.getElementById('new-capacity').value = 1;
    await refresh();
});

async function deleteSlot(id) {
    const res = await fetch(`/api/slots?id=${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (!res.ok) {
        const data = await res.json();
        alert(data.error || 'Impossible de supprimer ce creneau');
        return;
    }
    await refresh();
}

function openBookModal(slot) {
    currentBookingSlotId = slot.id;
    bookSlotLabel.textContent = `${formatDate(slot.date)} a ${slot.time}`;
    bookError.textContent = '';
    bookForm.reset();
    bookModal.classList.remove('hidden');
}

function closeBookModal() {
    bookModal.classList.add('hidden');
    currentBookingSlotId = null;
}

bookCancel.addEventListener('click', closeBookModal);

bookForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    bookError.textContent = '';
    const name = document.getElementById('book-name').value.trim();
    const email = document.getElementById('book-email').value.trim();

    const res = await fetch('/api/book', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slotId: currentBookingSlotId, name, email })
    });
    const data = await res.json();
    if (!res.ok) {
        bookError.textContent = data.error || 'Impossible de reserver ce creneau';
        return;
    }
    closeBookModal();
    await refresh();
});

filterDate.addEventListener('change', refresh);
clearFilterBtn.addEventListener('click', () => {
    filterDate.value = '';
    refresh();
});

refresh();
