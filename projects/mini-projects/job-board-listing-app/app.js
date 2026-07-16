const searchInput = document.getElementById("searchInput");
const locationFilter = document.getElementById("locationFilter");
const typeFilter = document.getElementById("typeFilter");
const jobList = document.getElementById("jobList");
const resultCount = document.getElementById("resultCount");
const emptyMessage = document.getElementById("emptyMessage");

function populateFilters() {
  const locations = [...new Set(JOBS.map(j => j.location))].sort();
  const types = [...new Set(JOBS.map(j => j.type))].sort();

  locations.forEach(loc => {
    const opt = document.createElement("option");
    opt.value = loc;
    opt.textContent = loc;
    locationFilter.appendChild(opt);
  });

  types.forEach(type => {
    const opt = document.createElement("option");
    opt.value = type;
    opt.textContent = type;
    typeFilter.appendChild(opt);
  });
}

function escapeHtml(s) {
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}

function render() {
  const search = searchInput.value.trim().toLowerCase();
  const location = locationFilter.value;
  const type = typeFilter.value;

  const filtered = JOBS.filter(job => {
    const matchesSearch = !search ||
      job.title.toLowerCase().includes(search) ||
      job.company.toLowerCase().includes(search) ||
      job.desc.toLowerCase().includes(search);
    const matchesLocation = !location || job.location === location;
    const matchesType = !type || job.type === type;
    return matchesSearch && matchesLocation && matchesType;
  });

  resultCount.textContent = `${filtered.length} offre(s) trouvée(s)`;

  jobList.innerHTML = "";
  filtered.forEach(job => {
    const card = document.createElement("div");
    card.className = "job-card";
    card.innerHTML = `
      <h2>${escapeHtml(job.title)}</h2>
      <div class="company">${escapeHtml(job.company)}</div>
      <div class="meta">
        <span class="tag">${escapeHtml(job.location)}</span>
        <span class="tag type">${escapeHtml(job.type)}</span>
      </div>
      <p class="desc">${escapeHtml(job.desc)}</p>
    `;
    jobList.appendChild(card);
  });

  emptyMessage.classList.toggle("hidden", filtered.length > 0);
}

searchInput.addEventListener("input", render);
locationFilter.addEventListener("change", render);
typeFilter.addEventListener("change", render);

populateFilters();
render();
