const dropZone = document.getElementById('dropZone');
const fileInput = document.getElementById('fileInput');
const filesBody = document.getElementById('filesBody');

document.addEventListener('DOMContentLoaded', loadFiles);

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) uploadFile(e.target.files[0]);
});

dropZone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropZone.classList.add('dragover');
});

dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));

dropZone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropZone.classList.remove('dragover');
    if (e.dataTransfer.files.length > 0) uploadFile(e.dataTransfer.files[0]);
});

async function loadFiles() {
    try {
        const res = await fetch('/api/files');
        const files = await res.json();
        if (!Array.isArray(files) || files.length === 0) {
            filesBody.innerHTML = '<tr><td colspan="4" class="empty">Aucun fichier stocké pour le moment.</td></tr>';
            return;
        }
        filesBody.innerHTML = files.map(f => `
            <tr>
                <td><strong>${escapeHtml(f.name)}</strong></td>
                <td>${formatSize(f.size)}</td>
                <td>${new Date(f.modified).toLocaleString()}</td>
                <td>
                    <a class="btn-download" href="/api/download?name=${encodeURIComponent(f.name)}">Télécharger</a>
                    <button class="btn btn-danger" onclick="deleteFile('${escapeHtml(f.name)}')">Supprimer</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        filesBody.innerHTML = '<tr><td colspan="4" class="empty">Erreur de chargement.</td></tr>';
    }
}

async function uploadFile(file) {
    if (file.size > 10 * 1024 * 1024) {
        alert('Le fichier est trop volumineux (max 10 Mo).');
        return;
    }
    const formData = new FormData();
    formData.append('file', file);

    try {
        const res = await fetch('/api/upload', {
            method: 'POST',
            body: formData
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Erreur d upload');
        fileInput.value = '';
        loadFiles();
    } catch (err) {
        alert('Échec de l upload: ' + err.message);
    }
}

async function deleteFile(name) {
    if (!confirm(`Supprimer le fichier "${name}" ?`)) return;
    try {
        const res = await fetch(`/api/files?name=${encodeURIComponent(name)}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Erreur suppression');
        loadFiles();
    } catch (err) {
        alert('Échec de la suppression: ' + err.message);
    }
}

function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
}

function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
