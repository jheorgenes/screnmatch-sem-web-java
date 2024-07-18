package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
  private Scanner leitura = new Scanner(System.in);
  private ConsumoAPI consumoApi = new ConsumoAPI();
  private ConverteDados conversor = new ConverteDados();
  private final String URL_BASE = "https://www.omdbapi.com/?apikey=308b77e3&t=";
  private List<DadosSerie> dadosSeries = new ArrayList<>();
  private SerieRepository repository;
  private List<Serie> series = new ArrayList<>();
  private Optional<Serie> serieBusca;

  public Principal(SerieRepository repository) {
    this.repository = repository;
  }

  private void buscarSerieWeb() {
    DadosSerie dados = getDadosSerie();
    Serie serie = new Serie(dados);
    repository.save(serie);
    System.out.println(dados);
  }

  private DadosSerie getDadosSerie() {
    System.out.println("Digite o nome da série para buscar");
    var nomeSerie = leitura.nextLine();
    var json = consumoApi.obterDados(URL_BASE + nomeSerie.replace(" ", "+"));
    DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
    return dados;
  }

  private void listarSeriesBuscadas() {
    series = repository.findAll();
//    series = dadosSeries.stream()
//                    .map(d -> new Serie(d))
//                    .collect(Collectors.toList());
    series.stream()
            .sorted(Comparator.comparing(Serie::getGenero))
            .forEach(System.out::println);
  }

  private void buscarEpisodioPorSerie() {
//    DadosSerie dadosSerie = getDadosSerie();
    listarSeriesBuscadas();
    System.out.println("Escolha uma série pelo nome: ");
    var nomeSerie = leitura.nextLine();

//    Optional<Serie> serie = series.stream()
//            .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//            .findFirst();

    Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

    if(serie.isPresent()) {
      var serieEncontrada = serie.get();
      List<DadosTemporada> temporadas = new ArrayList<>();

      for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
        var json = consumoApi.obterDados(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i);
        DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
        temporadas.add(dadosTemporada);
      }
      temporadas.forEach(System.out::println);

      List<Episodio> episodios = temporadas.stream()
              .flatMap(d -> d.episodios().stream()
                      .map(e -> new Episodio(d.numero(), e))
              ).collect(Collectors.toList());

      serieEncontrada.setEpisodios(episodios);

      repository.save(serieEncontrada);

    } else {
      System.out.println("Série não encontrada!");
    }

  }

  private void buscarSeriePorTitulo() {
    System.out.println("Escolha uma série pelo nome: ");
    var nomeSerie = leitura.nextLine();

    serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);
    if(serieBusca.isPresent()) {
      System.out.println("Dados da série: " + serieBusca.get());
    } else {
      System.out.println("Serie não encontrada!");
    }
  }

  private void buscarSeriesPorAtor() {
    System.out.println("Qual o nome para buscar? ");
    var nomeAtor = leitura.nextLine();
    System.out.println("Avaliações a partir de qual valor? ");
    var avaliacao = leitura.nextDouble();
    List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
    System.out.println("Séries em que " + nomeAtor + " trabalhou: ");
    seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + ", avaliação: " + s.getAvaliacao()));
  }

  private void buscarTop5Series() {
    List<Serie> serieTop = repository.findTop5ByOrderByAvaliacaoDesc();
    serieTop.forEach(s -> System.out.println(s.getTitulo() + ", avaliação: " + s.getAvaliacao()));
  }

  private void buscarSeriesPorCategoria() {
    System.out.println("Deseja buscar séries de que categoria/gênero? ");
    var nomeGenero = leitura.nextLine();
    Categoria categoria = Categoria.fromPortugues(nomeGenero);
    List<Serie> seriesPorCategoria = repository.findByGenero(categoria);
    System.out.println("Séries de categoria: " + nomeGenero);
    seriesPorCategoria.forEach(System.out::println);
  }

  private void filtrarSeriesPorTemporadaEAvaliacao() {
    System.out.println("Filtrar séries ate´quantas temporadas? ");
    var totalTemporadas = leitura.nextInt();
    leitura.nextLine();
    System.out.println("Com avaliação a partir de que valor? ");
    var avaliacao = leitura.nextDouble();
    leitura.nextLine();
//    List<Serie> filtroSeries = repository.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas, avaliacao);
    List<Serie> filtroSeries = repository.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);
    System.out.println("*** Séries filtradas ***");
    filtroSeries.forEach(s -> System.out.println(s.getTitulo() + " - avaliação: " + s.getAvaliacao()));
  }

  private void buscarEpisodioPorTrecho() {
    System.out.println("Qual o nome do episódio para avaliação?");
    var trecoEpisodio = leitura.nextLine();
    List<Episodio> episodiosEncontrados = repository.episodiosPorTrecho(trecoEpisodio);
    episodiosEncontrados.forEach(e ->
            System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(),
                    e.getNumero(), e.getTitulo()));
  }

  private void topEpisodiosPorSerie() {
    buscarSeriePorTitulo();
    if(serieBusca.isPresent()) {
      Serie serie = serieBusca.get();
      List<Episodio> topEpisodios = repository.topEpisodiosPorSerie(serie);
      topEpisodios.forEach(e ->
              System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                      e.getSerie().getTitulo(), e.getTemporada(),
                      e.getNumero(), e.getTitulo()));
    }
  }

  private void buscarEpisodiosDepoisDeUmaData() {
    buscarSeriePorTitulo();
    if(serieBusca.isPresent()) {
      Serie serie = serieBusca.get();
      System.out.println("Digite o ano limite de lançamento");
      var anoLancamento = leitura.nextInt();
      leitura.nextLine();
      List<Episodio> episodiosAno = repository.episodioPorSerieEAno(serie, anoLancamento);
      episodiosAno.forEach(System.out::println);
    }
  }

  public void exibeMenu() {

    var opcao = -1;
    while (opcao != 0) {
      var menu = """
              1 - Buscar séries
              2 - Buscar episódios
              3 - Listar séries buscadas
              4 - Buscar série por Título
              5 - Buscar séries por Ator
              6 - Buscar Top 5 séries
              7 - Buscar séries por categoria
              8 - Filtrar séries
              9 - Buscar episódios da série por Trecho
              10 - Buscar Top Episódios pela série
              11 - Buscar Episódios a partir de uma data
                            
              0 - Sair
              """;

      System.out.println(menu);
      opcao = leitura.nextInt();
      leitura.nextLine();

      switch (opcao) {
        case 1:
          buscarSerieWeb();
          break;
        case 2:
          buscarEpisodioPorSerie();
          break;
        case 3:
          listarSeriesBuscadas();
          break;
        case 4:
          buscarSeriePorTitulo();
          break;
        case 5:
          buscarSeriesPorAtor();
          break;
        case 6:
          buscarTop5Series();
          break;
        case 7:
          buscarSeriesPorCategoria();
          break;
        case 8:
          filtrarSeriesPorTemporadaEAvaliacao();
          break;
        case 9:
          buscarEpisodioPorTrecho();
          break;
        case 10:
          topEpisodiosPorSerie();
          break;
        case 11:
          buscarEpisodiosDepoisDeUmaData();
          break;
        case 0:
          System.out.println("Saindo...");
          break;
        default:
          System.out.println("Opção inválida");
      }
    }
//    var nomeSerie = leitura.nextLine();
//    var json = consumoApi.obterDados(URL_BASE + nomeSerie.replace(" ", "+"));
//    DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
//    System.out.println(dados);
//
//    List<DadosTemporada> temporadas = new ArrayList<>();
//    for (int i = 1; i <= dados.totalTemporadas(); i++) {
//      json = consumoApi.obterDados(URL_BASE + nomeSerie.replace(" ", "+") + "&season=" + i);
//      DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
//      temporadas.add(dadosTemporada);
//    }
//    temporadas.forEach(System.out::println);
//
//    for (int i = 0; i < dados.totalTemporadas(); i++) {
//      List<DadosEpisodios> episodiosTemporada = temporadas.get(i).episodios();
//      for (int j = 0; j < episodiosTemporada.size(); j++) {
//        System.out.println(episodiosTemporada.get(j).titulo());
//      }
//    }
//    temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
//
//    List<DadosEpisodios> dadosEpisodios = temporadas.stream()
//            .flatMap(t -> t.episodios().stream()) //Transforma várias listas em uma lista só. Em cada temporada há uma lista de episódios. Estou unificando todos esses episódios em uma lista só.
//            //.toList(); //Retorna uma lista imutavel
//            .collect(Collectors.toList()); //Retorna uma lista mutável
//
////    System.out.println("\nTop 10 episódios");
////    dadosEpisodios.stream()
////            .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")) //Filtra retirando todas as avaliações que forem "N/A" (Desconsiderando maíusculas e minúsculas)
////            .peek(e -> System.out.println("Primeiro filtro (N/A)"))  //
////            .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed()) //Ordena a lista comparando pelo campo avaliação e de forma decrescente (reserved)
////            .peek(e -> System.out.println("Ordenação " + e))
////            .limit(10) //Limita em 5 episódios
////            .peek(e -> System.out.println("Limite " + e))
////            .map(e -> e.titulo().toUpperCase())
////            .peek(e -> System.out.println("Mapeamento " + e))
////            .forEach(System.out::println);
//
//    List<Episodio> episodios = temporadas.stream()
//            .flatMap(t -> t.episodios().stream() //Transforma várias listas em uma lista só. Em cada temporada há uma lista de episódios. Estou unificando todos esses episódios em uma lista só.
//                    .map(d -> new Episodio(t.numero(), d)) //Converte a lista de episódios recebida em uma lista do tipo de classe Episódio (recebendo o número do episódio e o episódio em sí como argumento)
//            ).collect(Collectors.toList()); //Devolve em uma nova lista mutável.
//
//    episodios.forEach(System.out::println);
//
////    System.out.println("Digite um trecho do titulo do episódio");
////    var trecoTitulo = leitura.nextLine();
////    Optional<Episodio> episodioBuscado = episodios.stream()
////            .filter(e -> e.getTitulo().toUpperCase().contains(trecoTitulo.toUpperCase()))
////            .findFirst();
////    if(episodioBuscado.isPresent()) {
////      System.out.println("Episódio encontrado!");
////      System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
////    } else {
////      System.out.println("Episódio não encontrado");
////    }
//
////
////    System.out.println("A partir de que ano você deseja ver os episódios?");
////    var ano = leitura.nextInt();
////    leitura.nextLine();
////
////    LocalDate dataBusca = LocalDate.of(ano, 1, 1); //Sempre pegará do início do ano informado
////
////    DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
////    episodios.stream()
////            .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca)) //Filtra todos os episódios que forem após a data recebida como argumento
////            .forEach(e -> System.out.println("Temporada: " + e.getTemporada() + ", Episódio: " + e.getTitulo() + ", Data lançamento: " + e.getDataLancamento().format(formatador)));
////
//////    List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//////    nomes.stream()
//////            .sorted() //Ordenação
//////            .limit(3) //Limita a quantidade de Exibição dos dados
//////            .filter(n -> n.startsWith("N")) //Filtra dos dados obtidos, quem começa com a letra N
//////            .map(n -> n.toUpperCase()) //Refaz a lista de string transformando todos em letra maiúscula
//////            .forEach(System.out::println); //Imprime
//
//    Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
//            .filter(e -> e.getAvaliacao() > 0.0)
//            .collect(Collectors.groupingBy(Episodio::getTemporada, //Agrupa por Temporada (recebendo o agrupador e o conteúdo a ser agrupado como argumento)
//                     Collectors.averagingDouble(Episodio::getAvaliacao))); //Pega a média de avaliação por episódio e retorna uma lista
//
//    System.out.println(avaliacoesPorTemporada);
//
//    DoubleSummaryStatistics est = episodios.stream()
//            .filter(e -> e.getAvaliacao() > 0.0)
//            .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
//
//    System.out.println(est);
//    System.out.println("Media: " + est.getAverage());
//    System.out.println("Melhor episódio: " + est.getMax());
//    System.out.println("Pior episódio: " + est.getMin());
//    System.out.println("Quantidade: " + est.getCount());
  }


}
