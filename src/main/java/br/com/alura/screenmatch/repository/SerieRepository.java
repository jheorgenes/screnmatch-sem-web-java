package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

  Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

  //Busca Series por Atores que Contém (nomeAtor) ignorando Maiúsculas e minusculas E a avaliação maior ou igual a (avaliação)
  List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, Double avaliacao);

  List<Serie> findTop5ByOrderByAvaliacaoDesc();

  List<Serie> findByGenero(Categoria categoria);

  List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, double avaliacao);

  @Query("select s from Serie s where s.totalTemporadas <= :totalTemporadas and s.avaliacao >= :avaliacao")
  List<Serie> seriesPorTemporadaEAvaliacao(int totalTemporadas, double avaliacao);

  @Query("select e from Serie s join s.episodios e where e.titulo ilike %:trecoEpisodio%")
  List<Episodio> episodiosPorTrecho(String trecoEpisodio);

  @Query("select e from Serie s join s.episodios e where s = :serie order by e.avaliacao DESC LIMIT 5")
  List<Episodio> topEpisodiosPorSerie(Serie serie);

  @Query("select e from Serie s join s.episodios e where s = :serie and year(e.dataLancamento) >= :anoLancamento")
  List<Episodio> episodioPorSerieEAno(Serie serie, int anoLancamento);

  List<Serie> findTop5ByOrderByEpisodiosDataLancamentoDesc();

  @Query("SELECT s FROM Serie s " +
          "JOIN s.episodios e " +
          "GROUP BY s " +
          "ORDER BY MAX(e.dataLancamento) DESC LIMIT 5")
  List<Serie> encontrarEpisodiosMaisRecentes();

  @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.id = :id AND e.temporada = :numero")
  List<Episodio> obterEpisodiosPorTemporada(Long id, Long numero);

}
