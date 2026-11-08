package bancoalimentos;

import bancoalimentos.dao.DoacaoAlimentoDAO;
import bancoalimentos.dao.DoadorDAO;
import bancoalimentos.model.DoacaoAlimento;
import bancoalimentos.model.Doador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menu de console interativo do Sistema de Banco de Alimentos. */
public class Main {

    // ---------- Estilo ----------
    private static final String RESET    = "\u001B[0m";
    private static final String NEGRITO  = "\u001B[1m";
    private static final String CINZA    = "\u001B[90m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String VERDE    = "\u001B[32m";
    private static final String AMARELO  = "\u001B[33m";
    private static final String CIANO    = "\u001B[36m";
    private static final int LARGURA = 60;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Scanner entrada = new Scanner(System.in);
    private static final DoadorDAO doadorDAO = new DoadorDAO();
    private static final DoacaoAlimentoDAO doacaoDAO = new DoacaoAlimentoDAO();


    public static void main(String[] args) {
        boasVindas();

        int opcao;

        do {
            limparTela();
            exibirMenu();

            opcao = lerInt(CIANO + "  ❯ " + RESET + "Escolha uma opção: ");

            boolean voltar = false;

            if (opcao != 0) {
                limparTela();
            }

            try {
                executar(opcao);

            } catch (VoltarMenuException e) {
                voltar = true;

            } catch (IllegalArgumentException | IllegalStateException e) {
                aviso(e.getMessage());

            } catch (SQLException e) {
                erro("Erro de banco de dados: " + e.getMessage());
            }

            // Só pausa quando não estiver voltando ao menu.
            if (opcao != 0 && !voltar) {
                pausar();
            }

        } while (opcao != 0);

        System.out.println();
        sucesso("Até logo! Obrigado por ajudar a combater a fome. ♥");
    }

    // ---------- Menu ----------

    private static void exibirMenu() {
        String data = LocalDate.now().format(FORMATO);

        System.out.println(CIANO + "╔" + "═".repeat(LARGURA) + "╗" + RESET);

        System.out.println(borda(CINZA + " Digite VOLTAR a qualquer momento para retornar ao Menu" + RESET));
        secao("DOADORES");
        item("1", "Cadastrar doador");
        item("2", "Listar doadores");
        item("3", "Atualizar doador");
        item("4", "Remover doador");

        secao("DOAÇÕES");
        item("5", "Registrar doação");
        item("6", "Histórico de doações de um doador");
        item("7", "Alterar doação");
        item("8", "Marcar doação como distribuída");

        secao("RELATÓRIOS");
        item("9", "Alimentos próximos do vencimento (até 7 dias)");
        item("10", "Listar alimentos por data de validade");
        item("11", "Total de alimentos registrados (kg)");

        System.out.println(CIANO + "╟" + "─".repeat(LARGURA) + "╢" + RESET);
        System.out.println(borda(VERMELHO + NEGRITO + "0  " + RESET + "›  Sair"));
        System.out.println(CIANO + "╚" + "═".repeat(LARGURA) + "╝" + RESET);
    }

    private static void executar(int opcao) throws SQLException {
        switch (opcao) {
            case 1 -> cadastrarDoador();
            case 2 -> listarDoadores();
            case 3 -> atualizarDoador();
            case 4 -> removerDoador();
            case 5 -> registrarDoacao();
            case 6 -> historicoDoador();
            case 7 -> alterarDoacao();
            case 8 -> distribuirDoacao();

            case 9 -> {
                titulo("Alimentos próximos do vencimento");
                imprimir(doacaoDAO.listarProximasDoVencimento(DoacaoAlimento.DIAS_ALERTA_VENCIMENTO));
            }
            case 10 -> {
                titulo("Alimentos por data de validade");
                imprimir(doacaoDAO.listarPorValidade());
            }
            case 11 -> {
                titulo("Total de alimentos");
                carregar("Calculando");
                System.out.println("  Total registrado: " + NEGRITO + VERDE
                        + String.format("%.2f kg", doacaoDAO.somarTotalKg()) + RESET);
            }
            case 0 -> { }
            default -> aviso("Opção inválida. Escolha um número do menu.");
        }
    }

    // ---------- Doadores ----------

    private static void cadastrarDoador() throws SQLException {
        titulo("Cadastrar doador");
        String nome = lerObrigatorio("Nome: ", "O nome");
        String tipo = lerTipoDoador();
        String telefone = lerObrigatorio("Telefone: ", "O telefone");
        String email = lerObrigatorio("E-mail: ", "O e-mail");
        Doador doador = new Doador(0, nome, tipo, telefone, email);
        carregar("Salvando");
        doadorDAO.inserir(doador);
        sucesso("Doador cadastrado!");
        System.out.println("  " + doador.apresentar());
    }

    private static void listarDoadores() throws SQLException {
        titulo("Doadores cadastrados");
        List<Doador> doadores = doadorDAO.listar();
        if (doadores.isEmpty()) {
            info("Nenhum doador cadastrado.");
            return;
        }
        List<String[]> linhas = new ArrayList<>();
        for (Doador d : doadores) {
            linhas.add(new String[]{
                    String.valueOf(d.getId()), d.getNome(), d.getTipoDoador(),
                    d.getTelefone(), d.getEmail()});
        }
        tabela(new String[]{"ID", "Nome", "Tipo", "Telefone", "E-mail"},
                new int[]{4, 18, 16, 14, 24}, linhas);
        System.out.println(CINZA + "  " + doadores.size() + " doador(es)." + RESET);
    }



    private static void atualizarDoador() throws SQLException {
        titulo("Atualizar doador");

        Doador doador = buscarDoadorOuAvisar(
                lerInt("ID do doador: ")
        );

        if (doador == null) {
            return;
        }

        info("Dados atuais: " + doador.apresentar());

        System.out.println("\n  Selecione os campos que deseja atualizar:");
        System.out.println("  1 - Nome");
        System.out.println("  2 - Tipo de doador");
        System.out.println("  3 - Telefone");
        System.out.println("  4 - E-mail");

        System.out.println("\n  Digite as opções separadas por vírgula.");
        System.out.println("  Exemplo: 1, 3, 4");

        String[] opcoes = lerTexto(
                prompt("Opções: ")
        ).split(",");

        boolean nome = false;
        boolean tipo = false;
        boolean telefone = false;
        boolean email = false;

        for (String opcao : opcoes) {
            switch (opcao.trim()) {
                case "1" -> nome = true;
                case "2" -> tipo = true;
                case "3" -> telefone = true;
                case "4" -> email = true;
                default -> aviso("Opção ignorada: " + opcao.trim());
            }
        }

        if (!nome && !tipo && !telefone && !email) {
            aviso("Nenhum campo válido foi selecionado.");
            return;
        }

        System.out.println("\n  Informe os novos dados:");

        if (nome) {
            doador.setNome(
                    lerObrigatorio("Novo nome: ", "O nome")
            );
        }

        if (tipo) {
            doador.setTipoDoador(lerTipoDoador());
        }

        if (telefone) {
            doador.setTelefone(
                    lerObrigatorio("Novo telefone: ", "O telefone")
            );
        }

        if (email) {
            doador.setEmail(
                    lerObrigatorio("Novo e-mail: ", "O e-mail")
            );
        }

        carregar("Atualizando");
        doadorDAO.atualizar(doador);

        sucesso("Doador atualizado com sucesso!");
    }



    private static void removerDoador() throws SQLException {
        titulo("Remover doador");
        int id = lerInt("ID do doador: ");
        Doador doador = buscarDoadorOuAvisar(id);
        if (doador == null) {
            return;
        }
        info("Doador: " + doador.apresentar());
        if (!confirmar("Tem certeza que deseja remover?")) {
            aviso("Remoção cancelada.");
            return;
        }
        carregar("Removendo");
        doadorDAO.excluir(id);
        sucesso("Doador removido.");
    }

    // ---------- Doações ----------

    private static void registrarDoacao() throws SQLException {
        titulo("Registrar doação");
        Doador doador = buscarDoadorOuAvisar(lerInt("ID do doador: "));
        if (doador == null) {
            return;
        }
        info("Doador: " + doador.apresentar());
        String tipoAlimento = lerObrigatorio("Tipo de alimento: ", "O tipo de alimento");
        double quantidade = lerQuantidadeKg("Quantidade (kg): ");
        LocalDate validade = lerData("Validade (dd/MM/aaaa): ");
        DoacaoAlimento doacao = new DoacaoAlimento(0, doador.getId(), LocalDate.now(),
                tipoAlimento, quantidade, validade, false);
        carregar("Registrando");
        doacaoDAO.inserir(doacao);
        sucesso("Doação registrada!");
        System.out.println("  " + doacao);
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), validade);
        if (dias <= DoacaoAlimento.DIAS_ALERTA_VENCIMENTO) {
            aviso("Atenção: este alimento vence em " + Math.max(dias, 0) + " dia(s)!");
        }
    }

    private static void historicoDoador() throws SQLException {
        titulo("Histórico de doações");
        Doador doador = buscarDoadorOuAvisar(lerInt("ID do doador: "));
        if (doador == null) {
            return;
        }
        for (DoacaoAlimento d : doacaoDAO.listarPorDoador(doador.getId())) {
            doador.adicionarDoacao(d);
        }
        System.out.println();
        System.out.println("  " + NEGRITO + doador.apresentar() + RESET);
        System.out.println();
        imprimir(doador.getDoacoes());
        System.out.println();
        System.out.println("  Total doado: " + NEGRITO + VERDE
                + String.format("%.2f kg", doador.getTotalDoadoKg()) + RESET);
    }


    private static void alterarDoacao() throws SQLException {
        titulo("Alterar doação");

        DoacaoAlimento doacao = doacaoDAO.buscarPorId(
                lerInt("ID da doação: ")
        );

        if (doacao == null) {
            aviso("Doação não encontrada.");
            return;
        }

        while (true) {
            info("Dados atuais: " + doacao);

            System.out.println("\n  O que deseja atualizar?");
            System.out.println("  1 - Tipo de alimento");
            System.out.println("  2 - Quantidade (kg)");
            System.out.println("  3 - Data de validade");
            System.out.println("  0 - Finalizar alterações");

            int opcao = lerInt("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> doacao.setTipoAlimento(
                        lerObrigatorio("Novo tipo de alimento: ",
                                "O tipo de alimento")
                );

                case 2 -> doacao.setQuantidadeKg(
                        lerQuantidadeKg("Nova quantidade (kg): ")
                );

                case 3 -> doacao.setValidade(
                        lerData("Nova validade (dd/MM/aaaa): ")
                );

                case 0 -> {
                    carregar("Atualizando");
                    doacaoDAO.atualizar(doacao);
                    sucesso("Doação atualizada com sucesso!");
                    return;
                }

                default -> aviso("Opção inválida.");
            }
        }
    }


    private static void distribuirDoacao() throws SQLException {
        titulo("Distribuir doação");
        DoacaoAlimento doacao = doacaoDAO.buscarPorId(lerInt("ID da doação: "));
        if (doacao == null) {
            aviso("Doação não encontrada.");
            return;
        }
        if (!DoacaoAlimento.DISPONIVEL.equals(doacao.getSituacao())) {
            aviso("Só é possível distribuir alimentos disponíveis. Situação atual: "
                    + doacao.getSituacao());
            return;
        }
        info("Doação: " + doacao);
        if (!confirmar("Marcar como distribuída?")) {
            aviso("Operação cancelada.");
            return;
        }
        carregar("Registrando distribuição");
        doacaoDAO.marcarComoDistribuida(doacao.getId());
        sucesso("Doação marcada como distribuída.");
    }

    // ---------- Apresentação ----------

    private static void boasVindas() {
        limparTela();
        System.out.println();
        System.out.println(CIANO + NEGRITO);
        System.out.println("   ____        _      _   _     ____   ___  ");
        System.out.println("  |  ()  \\    / \\    | \\ | |  / ___| /  _  \\ ");
        System.out.println("  |   _  /   / _ \\   |  \\| | | |     | | | |");
        System.out.println("  |  ()  \\  / ___ \\  | |\\  | | |___  | |_| |");
        System.out.println("  |______/ /_/   \\_\\ | _|\\_|  \\____|  \\___/ ");
        System.out.println(RESET + AMARELO + "          D E   A L I M E N T O S" + RESET);
        System.out.println();
    }

    private static void imprimir(List<DoacaoAlimento> doacoes) {
        if (doacoes.isEmpty()) {
            info("Nenhum registro encontrado.");
            return;
        }
        LocalDate hoje = LocalDate.now();
        List<String[]> linhas = new ArrayList<>();
        for (DoacaoAlimento d : doacoes) {
            long dias = ChronoUnit.DAYS.between(hoje, d.getValidade());
            String cor = dias < 0 ? VERMELHO : dias <= DoacaoAlimento.DIAS_ALERTA_VENCIMENTO ? AMARELO : VERDE;
            String situacao = DoacaoAlimento.DISPONIVEL.equals(d.getSituacao())
                    ? VERDE + d.getSituacao() + RESET : CINZA + d.getSituacao() + RESET;
            linhas.add(new String[]{
                    String.valueOf(d.getId()),
                    d.getTipoAlimento(),
                    String.format("%.2f", d.getQuantidadeKg()),
                    cor + d.getValidade().format(FORMATO) + RESET,
                    situacao});
        }
        tabela(new String[]{"ID", "Alimento", "Kg", "Validade", "Situação"},
                new int[]{4, 22, 9, 12, 14}, linhas);
        System.out.println(CINZA + "  Legenda de validade: " + RESET + VERDE + "ok " + RESET
                + AMARELO + "vence em breve " + RESET + VERMELHO + "vencido" + RESET);
    }

    /** Desenha uma tabela com bordas; as células podem conter códigos de cor. */
    private static void tabela(String[] cabecalho, int[] larguras, List<String[]> linhas) {
        System.out.println(CIANO + "  " + bordaTabela('┌', '┬', '┐', larguras) + RESET);
        StringBuilder sb = new StringBuilder("  " + CIANO + "│" + RESET);
        for (int i = 0; i < cabecalho.length; i++) {
            sb.append(" ").append(NEGRITO).append(ajustar(cabecalho[i], larguras[i])).append(RESET)
                    .append(" ").append(CIANO).append("│").append(RESET);
        }
        System.out.println(sb);
        System.out.println(CIANO + "  " + bordaTabela('├', '┼', '┤', larguras) + RESET);
        for (String[] linha : linhas) {
            StringBuilder l = new StringBuilder("  " + CIANO + "│" + RESET);
            for (int i = 0; i < linha.length; i++) {
                l.append(" ").append(ajustar(linha[i], larguras[i])).append(" ")
                        .append(CIANO).append("│").append(RESET);
            }
            System.out.println(l);
        }
        System.out.println(CIANO + "  " + bordaTabela('└', '┴', '┘', larguras) + RESET);
    }

    private static String bordaTabela(char esq, char meio, char dir, int[] larguras) {
        StringBuilder sb = new StringBuilder().append(esq);
        for (int i = 0; i < larguras.length; i++) {
            sb.append("─".repeat(larguras[i] + 2));
            sb.append(i < larguras.length - 1 ? meio : dir);
        }
        return sb.toString();
    }

    /** Corta ou completa com espaços, ignorando códigos de cor no cálculo. */
    private static String ajustar(String texto, int largura) {
        if (texto == null) {
            texto = "";
        }
        int visivel = semCor(texto).length();
        if (visivel > largura) {
            String puro = semCor(texto);
            return puro.substring(0, largura - 1) + "…";
        }
        return texto + " ".repeat(largura - visivel);
    }

    private static String semCor(String s) {
        return s.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(CIANO + NEGRITO + "  ▌ " + texto.toUpperCase() + RESET);
        System.out.println(CIANO + "  " + "─".repeat(LARGURA - 4) + RESET);
        System.out.println();
    }

    private static void secao(String nome) {
        System.out.println(CIANO + "╟" + "─".repeat(LARGURA) + "╢" + RESET);
        System.out.println(borda(AMARELO + NEGRITO + nome + RESET));
    }

    private static void item(String numero, String texto) {
        System.out.println(borda(VERDE + String.format("%-3s", numero) + RESET + "›  " + texto));
    }

    private static String borda(String conteudo) {
        int espacos = LARGURA - 2 - semCor(conteudo).length();
        return CIANO + "║" + RESET + "  " + conteudo + " ".repeat(Math.max(0, espacos))
                + CIANO + "║" + RESET;
    }

    private static String linhaCentro(String conteudo) {
        int visivel = semCor(conteudo).length();
        int esq = (LARGURA - visivel) / 2;
        int dir = LARGURA - visivel - esq;
        return CIANO + "║" + RESET + " ".repeat(esq) + conteudo + " ".repeat(dir) + CIANO + "║" + RESET;
    }

    // ---------- Mensagens e animações ----------

    private static void sucesso(String msg) { System.out.println(VERDE + "  ✔ " + msg + RESET); }
    private static void aviso(String msg)   { System.out.println(AMARELO + "  ⚠ " + msg + RESET); }
    private static void erro(String msg)    { System.out.println(VERMELHO + "  ✖ " + msg + RESET); }
    private static void info(String msg)    { System.out.println(CIANO + "  ! " + RESET + msg); }

    private static void limparTela() {
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }

    private static void pausar() {
        System.out.println();
        System.out.print(CINZA + "  Pressione ENTER para voltar ao menu..." + RESET);
        entrada.nextLine();
    }

    private static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Spinner rápido para dar feedback de que algo está acontecendo. */
    private static void carregar(String msg) {
        String[] quadros = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        for (int i = 0; i < 12; i++) {
            System.out.print("\r" + CIANO + "  " + quadros[i % quadros.length] + RESET + " " + msg + "...");
            System.out.flush();
            dormir(50);
        }
        System.out.print("\r" + " ".repeat(LARGURA) + "\r");
    }

    private static void barraProgresso(String msg) {
        int total = 30;
        for (int i = 0; i <= total; i++) {
            System.out.print("\r  " + msg + " " + CIANO + "█".repeat(i) + CINZA + "░".repeat(total - i)
                    + RESET + " " + (i * 100 / total) + "%");
            System.out.flush();
            dormir(25);
        }
        System.out.println();
        dormir(300);
    }

    // ---------- Entrada de dados ----------

    private static boolean confirmar(String pergunta) {
        while (true) {
            String r = lerTexto(AMARELO + "  ? " + RESET + pergunta + " (S/N): ").trim().toLowerCase();
            if (r.equals("s") || r.equals("sim")) {
                return true;
            }
            if (r.equals("n") || r.equals("nao") || r.equals("não")) {
                return false;
            }
            aviso("Responda com S ou N.");
        }
    }

    private static Doador buscarDoadorOuAvisar(int id) throws SQLException {
        Doador doador = doadorDAO.buscarPorId(id);
        if (doador == null) {
            aviso("Doador não encontrado.");
        }
        return doador;
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);

        String texto = entrada.nextLine();

        if (texto.trim().equalsIgnoreCase("VOLTAR")) {
            throw new VoltarMenuException();
        }

        return texto;
    }

    private static String prompt(String mensagem) {
        return CIANO + "  ❯ " + RESET + mensagem;
    }

    private static String lerTipoDoador() {
        while (true) {
            String resposta = lerTexto(prompt("Tipo (1 - Pessoa Física, 2 - Pessoa Jurídica): ")).trim();
            if (resposta.equals("1")) {
                return Doador.FISICA;
            }
            if (resposta.equals("2")) {
                return Doador.JURIDICA;
            }
            aviso("Digite 1 ou 2.");
        }
    }

    /** Repete a pergunta até o usuário informar um valor não vazio. */
    private static String lerObrigatorio(String mensagem, String campo) {
        while (true) {
            String valor = lerTexto(prompt(mensagem)).trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            aviso(campo + " é obrigatório. Tente novamente.");
        }
    }

    /** Repete a pergunta até a quantidade ser maior que zero. */
    private static double lerQuantidadeKg(String mensagem) {
        while (true) {
            double valor = lerDouble(mensagem);
            if (valor > 0) {
                return valor;
            }
            aviso("A quantidade (kg) deve ser maior que zero.");
        }
    }

    private static int lerInt(String mensagem) {
        String texto = mensagem.contains("❯") ? mensagem : prompt(mensagem);
        while (true) {
            try {
                return Integer.parseInt(lerTexto(texto).trim());
            } catch (NumberFormatException e) {
                erro("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            try {
                return Double.parseDouble(lerTexto(prompt(mensagem)).trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                erro("Digite um número válido (ex.: 12,5).");
            }
        }
    }

    private static LocalDate lerData(String mensagem) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(prompt(mensagem)).trim(), FORMATO);
            } catch (DateTimeParseException e) {
                erro("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }
    private static class VoltarMenuException extends RuntimeException {
    }
}