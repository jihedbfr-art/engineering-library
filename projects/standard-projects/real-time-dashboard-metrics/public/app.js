const cpuVal = document.getElementById('cpuValue');
const memVal = document.getElementById('memoryValue');
const rpsVal = document.getElementById('rpsValue');
const latVal = document.getElementById('latencyValue');
const canvas = document.getElementById('cpuChart');
const ctx = canvas.getContext('2d');

setInterval(fetchMetrics, 2000);
fetchMetrics();

async function fetchMetrics() {
    try {
        const res = await fetch('/api/metrics/history');
        const history = await res.json();
        if (!Array.isArray(history) || history.length === 0) return;

        const latest = history[history.length - 1];
        cpuVal.innerText = `${latest.cpu} %`;
        memVal.innerText = `${latest.memory} MB`;
        rpsVal.innerText = `${latest.rps} rps`;
        latVal.innerText = `${latest.latency} ms`;

        drawChart(history.map(h => h.cpu));
    } catch (e) {
        console.error('Fetch error:', e);
    }
}

function drawChart(values) {
    const width = canvas.width;
    const height = canvas.height;
    ctx.clearRect(0, 0, width, height);

    // Grid lines
    ctx.strokeStyle = '#1e293b';
    ctx.lineWidth = 1;
    for (let y = 50; y < height; y += 50) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
    }

    if (values.length < 2) return;

    const step = width / (60 - 1);
    ctx.beginPath();
    ctx.strokeStyle = '#38bdf8';
    ctx.lineWidth = 3;

    for (let i = 0; i < values.length; i++) {
        const x = i * step;
        const val = Math.min(100, Math.max(0, values[i]));
        const y = height - (val / 100) * (height - 20) - 10;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
    }
    ctx.stroke();

    // Fill area under line
    const lastX = (values.length - 1) * step;
    ctx.lineTo(lastX, height);
    ctx.lineTo(0, height);
    ctx.closePath();
    ctx.fillStyle = 'rgba(56, 189, 248, 0.1)';
    ctx.fill();
}
