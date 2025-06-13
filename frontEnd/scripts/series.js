import getDatos from "./getDatos.js";

const params = new URLSearchParams(window.location.search);
const serieId = params.get('id');
const listaTemporadas = document.getElementById('temporadas-select');
const fichaSerie = document.getElementById('temporadas-episodios');
const fichaDescripcion = document.getElementById('ficha-descripcion');

// Funcion para cargar temporadas
function cargarTemporadas() {
    getDatos(`/series/${serieId}/temporadas/todas`)
        .then(data => {
            const temporadasUnicas = [...new Set(data.map(temporada => temporada.temporada))];
            listaTemporadas.innerHTML = ''; // Limpia las opciones existentes

            const optionDefault = document.createElement('option');
            optionDefault.value = '';
            optionDefault.textContent = 'Seleccione la temporada';
            listaTemporadas.appendChild(optionDefault);

            temporadasUnicas.forEach(temporada => {
                const option = document.createElement('option');
                option.value = temporada;
                option.textContent = `Temporada ${temporada}`;
                listaTemporadas.appendChild(option);
            });

            const optionTodos = document.createElement('option');
            optionTodos.value = 'todas';
            optionTodos.textContent = 'Todas las temporadas';
            listaTemporadas.appendChild(optionTodos);

            const optionTop5 = document.createElement('option');
            optionTop5.value = 'top5';
            optionTop5.textContent = 'Top 5 episodios';
            listaTemporadas.appendChild(optionTop5);
        })
        .catch(error => {
            console.error('Error al obtener temporadas:', error);
        });
}


// Funcion para cargar episodios de una temporada
function cargarEpisodios() {
    const valorSeleccionado = listaTemporadas.value;
    fichaSerie.innerHTML = ''; // Limpiar contenido

    if (valorSeleccionado === 'top5') {
        cargarTop5Episodios(); // Redirige a la nueva función
        return;
    }

    // Resto de lógica para cargar temporadas
    getDatos(`/series/${serieId}/temporadas/${valorSeleccionado}`)
        .then(data => {
            const temporadasUnicas = [...new Set(data.map(t => t.temporada))];

            temporadasUnicas.forEach(temp => {
                const titulo = document.createElement('p');
                titulo.innerHTML = `<br>Temporada ${temp}<br><br>`;

                const ul = document.createElement('ul');
                ul.className = 'episodios-lista';

                const episodiosFiltrados = data.filter(e => e.temporada === temp);

                ul.innerHTML = episodiosFiltrados.map(e => `
                    <li>${e.numeroEpisodio} - ${e.titulo}</li>
                `).join('');

                fichaSerie.appendChild(titulo);
                fichaSerie.appendChild(ul);
            });
        })
        .catch(error => {
            console.error('Error al cargar episodios:', error);
        });
}


// Funcion para cargar informaciones de la serie
function cargarInfoSerie() {
    getDatos(`/series/${serieId}`)
        .then(data => {
            fichaDescripcion.innerHTML = `
                <img src="${data.poster}" alt="${data.titulo}" />
                <div>
                    <h2>${data.titulo}</h2>
                    <div class="descricao-texto">
                        <p><b>Média de evaluaciones:</b> ${data.evaluacion}</p>
                        <p><b>Sinopsis:</b> ${data.sinopsis}</p>
                        <p><b>Actores:</b> ${data.actores}</p>
                    </div>
                </div>
            `;
        })
        .catch(error => {
            console.error('Error al obtener informaciones de la serie:', error);
        });
}

function cargarTop5Episodios() {
    getDatos(`/series/${serieId}/top5`)
        .then(data => {
            const titulo = document.createElement('h3');
            titulo.innerHTML = 'Top 5 Episodios Mejor Evaluados<br><br>';
            fichaSerie.appendChild(titulo);

            const ul = document.createElement('ul');
            ul.className = 'episodios-lista';

            ul.innerHTML = data.map(ep => `
                <li>
                    Temporada ${ep.temporada} - Episodio ${ep.numeroEpisodio}: ${ep.titulo}
                </li>
            `).join('');

            fichaSerie.appendChild(ul);
        })
        .catch(error => {
            console.error('Error al obtener Top 5 episodios:', error);
        });
}



// Adiciona escuchador de evento para el elemento select
listaTemporadas.addEventListener('change', cargarEpisodios);

// Carga las informaciones de la série y las temporadas cuando la página carga
cargarInfoSerie();
cargarTemporadas();
