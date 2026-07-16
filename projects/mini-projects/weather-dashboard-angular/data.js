// Données météo SIMULÉES, codées en dur. Aucun appel réseau.
const WEATHER_DATA = {
  Paris: [
    { day: 'Lun', icon: '⛅', tempMax: 18, tempMin: 11, desc: 'Nuageux' },
    { day: 'Mar', icon: '🌧️', tempMax: 15, tempMin: 10, desc: 'Pluie' },
    { day: 'Mer', icon: '☀️', tempMax: 21, tempMin: 12, desc: 'Ensoleillé' },
    { day: 'Jeu', icon: '☀️', tempMax: 23, tempMin: 14, desc: 'Ensoleillé' },
    { day: 'Ven', icon: '⛅', tempMax: 19, tempMin: 13, desc: 'Nuageux' },
    { day: 'Sam', icon: '🌦️', tempMax: 17, tempMin: 11, desc: 'Averses' },
    { day: 'Dim', icon: '☀️', tempMax: 20, tempMin: 12, desc: 'Ensoleillé' }
  ],
  Tunis: [
    { day: 'Lun', icon: '☀️', tempMax: 29, tempMin: 20, desc: 'Ensoleillé' },
    { day: 'Mar', icon: '☀️', tempMax: 31, tempMin: 21, desc: 'Ensoleillé' },
    { day: 'Mer', icon: '⛅', tempMax: 27, tempMin: 19, desc: 'Nuageux' },
    { day: 'Jeu', icon: '☀️', tempMax: 30, tempMin: 20, desc: 'Ensoleillé' },
    { day: 'Ven', icon: '🌬️', tempMax: 26, tempMin: 18, desc: 'Venteux' },
    { day: 'Sam', icon: '☀️', tempMax: 28, tempMin: 19, desc: 'Ensoleillé' },
    { day: 'Dim', icon: '☀️', tempMax: 32, tempMin: 22, desc: 'Ensoleillé' }
  ],
  Londres: [
    { day: 'Lun', icon: '🌧️', tempMax: 14, tempMin: 8, desc: 'Pluie' },
    { day: 'Mar', icon: '🌧️', tempMax: 13, tempMin: 7, desc: 'Pluie' },
    { day: 'Mer', icon: '⛅', tempMax: 15, tempMin: 9, desc: 'Nuageux' },
    { day: 'Jeu', icon: '🌦️', tempMax: 14, tempMin: 8, desc: 'Averses' },
    { day: 'Ven', icon: '⛅', tempMax: 16, tempMin: 9, desc: 'Nuageux' },
    { day: 'Sam', icon: '☀️', tempMax: 18, tempMin: 10, desc: 'Ensoleillé' },
    { day: 'Dim', icon: '⛅', tempMax: 15, tempMin: 9, desc: 'Nuageux' }
  ],
  Tokyo: [
    { day: 'Lun', icon: '☀️', tempMax: 24, tempMin: 17, desc: 'Ensoleillé' },
    { day: 'Mar', icon: '⛅', tempMax: 22, tempMin: 16, desc: 'Nuageux' },
    { day: 'Mer', icon: '🌧️', tempMax: 19, tempMin: 15, desc: 'Pluie' },
    { day: 'Jeu', icon: '🌧️', tempMax: 20, tempMin: 15, desc: 'Pluie' },
    { day: 'Ven', icon: '⛅', tempMax: 23, tempMin: 16, desc: 'Nuageux' },
    { day: 'Sam', icon: '☀️', tempMax: 25, tempMin: 18, desc: 'Ensoleillé' },
    { day: 'Dim', icon: '☀️', tempMax: 26, tempMin: 18, desc: 'Ensoleillé' }
  ]
};
