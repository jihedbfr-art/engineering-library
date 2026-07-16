const cities = Object.keys(WEATHER_DATA);
let currentCity = cities[0];

const citySelector = document.getElementById('city-selector');
const forecastEl = document.getElementById('forecast');

function renderCitySelector() {
  citySelector.innerHTML = '';
  cities.forEach(city => {
    const btn = document.createElement('button');
    btn.textContent = city;
    btn.className = city === currentCity ? 'active' : '';
    btn.addEventListener('click', () => {
      currentCity = city;
      renderCitySelector();
      renderForecast();
    });
    citySelector.appendChild(btn);
  });
}

function renderForecast() {
  forecastEl.innerHTML = '';
  const days = WEATHER_DATA[currentCity];
  days.forEach(d => {
    const card = document.createElement('div');
    card.className = 'day-card';
    card.innerHTML = `
      <div class="day-name">${d.day}</div>
      <div class="icon">${d.icon}</div>
      <div class="temp">${d.tempMax}°</div>
      <div class="temp-min">${d.tempMin}°</div>
      <div class="desc">${d.desc}</div>
    `;
    forecastEl.appendChild(card);
  });
}

renderCitySelector();
renderForecast();
