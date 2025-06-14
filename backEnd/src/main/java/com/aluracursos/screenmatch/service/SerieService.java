package com.aluracursos.screenmatch.service;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.dto.SerieDTO;
import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository repository;

    @Autowired
    private ConsultaGemini consultaGemini;

    private final Map<Long, String> cacheSinopsisTraducidas = new ConcurrentHashMap<>();

    public List<SerieDTO> obtenerTodasLasSeries() {
        return convierteDatos(repository.findAll());
    }

    public List<SerieDTO> obtenerTop5() {
        return convierteDatos(repository.findTop5ByOrderByEvaluacionDesc());
    }

    public List<SerieDTO> obtenerLanzamientosMasRecientes() {
        return convierteDatos(repository.lanzamientosMasRecientes());
    }

    public List<SerieDTO> convierteDatos(List<Serie> series) {
        return series.stream()
                .map(s -> new SerieDTO(
                        s.getId(),
                        s.getTitulo(),
                        s.getTotalTemporadas(),
                        s.getEvaluacion(),
                        s.getPoster(),
                        s.getGenero(),
                        s.getActores(),
                        obtenerSinopsisTraducida(s.getId(), s.getSinopsis())
                ))
                .collect(Collectors.toList());
    }

    public SerieDTO obtenerPorId(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()) {
            Serie s = serie.get();
            return new SerieDTO(
                    s.getId(),
                    s.getTitulo(),
                    s.getTotalTemporadas(),
                    s.getEvaluacion(),
                    s.getPoster(),
                    s.getGenero(),
                    s.getActores(),
                    obtenerSinopsisTraducida(s.getId(), s.getSinopsis())
            );
        }
        return null;
    }

    public List<EpisodioDTO> obtenerTodasLasTemporadas(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()) {
            Serie s = serie.get();
            return s.getEpisodios().stream()
                    .map(e -> new EpisodioDTO(
                            e.getTemporada(),
                            e.getTitulo(),
                            e.getNumeroEpisodio()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> obtenerTemporadasPorNumero(Long id, Long numeroTemporada) {
        return repository.obtenerTemporadasPorNumero(id, numeroTemporada)
                .stream()
                .map(e -> new EpisodioDTO(
                        e.getTemporada(),
                        e.getTitulo(),
                        e.getNumeroEpisodio()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obtenerSeriesPorCategoria(String nombreGenero) {
        Categoria categoria = Categoria.fromEspanol(nombreGenero);
        return convierteDatos(repository.findByGenero(categoria));
    }

    public List<EpisodioDTO> obtenerTemporadasPorTop5(Long id) {
        return repository.top5Episodios(repository.findById(id).orElse(null))
                .stream()
                .map(e -> new EpisodioDTO(
                        e.getTemporada(),
                        e.getTitulo(),
                        e.getNumeroEpisodio()))
                .collect(Collectors.toList());
    }


    private String obtenerSinopsisTraducida(Long serieId, String sinopsisOriginal) {

        if (cacheSinopsisTraducidas.containsKey(serieId)) {
            return cacheSinopsisTraducidas.get(serieId);
        }


        if (sinopsisOriginal.matches(".*[áéíóúñÁÉÍÓÚÑ].*")) {
            return sinopsisOriginal;
        }

        String traducida = consultaGemini.obtenerTraduccion(sinopsisOriginal);
        if (traducida != null && !traducida.isBlank()) {
            cacheSinopsisTraducidas.put(serieId, traducida);
            return traducida;
        }

        return sinopsisOriginal;
    }
}
