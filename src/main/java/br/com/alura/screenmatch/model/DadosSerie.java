package br.com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) //Vai ignorar o restante do Json que não foi mapeado
public record DadosSerie(
        @JsonAlias("Title") String titulo,
        @JsonAlias("totalSeasons") Integer totalTemporadas,
        @JsonAlias("imdbRating") String avaliacao,
        @JsonAlias("Genre") String genero,
        @JsonAlias("Actors") String atores,
        @JsonAlias("Poster") String poster,
        @JsonAlias("Plot") String sinopse
//        @JsonProperty("imdbID") String id //@JsonProperty Serve tanto para Apelidar um Json lido, quanto para "Enviar" o Json com essa mesma descrição
) { }
