package bancoalimentos;

import bancoalimentos.dao.DoacaoAlimentoDAO;
import bancoalimentos.dao.DoadorDAO;
import bancoalimentos.model.DoacaoAlimento;
import bancoalimentos.model.Doador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Menu de console do Sistema de Banco de Alimentos. */
public class Main {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Scanner entrada = new Scanner(System.in);
    private static final DoadorDAO doadorDAO = new DoadorDAO();
    private static final DoacaoAlimentoDAO doacaoDAO = new DoacaoAlimentoDAO();

    public static void main(String[] args) {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInt("Escolha uma opção: ");
            try {
                executar(opcao);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Atenção: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            }
        } while (opcao != 0);
        System.out.println("Até logo!");
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("===== BANCO DE ALIMENTOS =====");
        System.out.println("1  - Cadastrar doador");
        System.out.println("2  - Listar doadores");
        System.out.println("3  - Atualizar doador");
        System.out.println("4  - Remover doador");
        System.out.println("5  - Registrar doação");
        System.out.println("6  - Histórico de doações de um doador");
        System.out.println("7  - Alimentos próximos do vencimento (até 7 dias)");
        System.out.println("8  - Listar alimentos por data de validade");
        System.out.println("9  - Total de alimentos registrados (kg)");
        System.out.println("10 - Alterar doação");
        System.out.println("11 - Marcar doação como distribuída");
        System.out.println("0  - Sair");
    }

    private static void executar(int opcao) throws SQLException {
        switch (opcao) {
            case 1 -> cadastrarDoador();
            case 2 -> listarDoadores();
            case 3 -> atualizarDoador();
            case 4 -> removerDoador();
            case 5 -> registrarDoacao();
            case 6 -> historicoDoador();
            case 7 -> imprimir(doacaoDAO.listarProximasDoVencimento(DoacaoAlimento.DIAS_ALERTA_VENCIMENTO));
            case 8 -> imprimir(doacaoDAO.listarPorValidade());
            case 9 -> System.out.printf("Total registrado: %.2f kg%n", doacaoDAO.somarTotalKg());
            case 10 -> alterarDoacao();
            case 11 -> distribuirDoacao();
            case 0 -> { }
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- Doadores ----------

    private static void cadastrarDoador() throws SQLException {
        String nome = lerObrigatorio("Nome: ", "O nome");
        String tipo = lerTipoDoador();
        String telefone = lerObrigatorio("Telefone: ", "O telefone");
        String email = lerObrigatorio("E-mail: ", "O e-mail");
        Doador doador = new Doador(0, nome, tipo, telefone, email);
        doadorDAO.inserir(doador);
        System.out.println("Doador cadastrado: " + doador.apresentar());
    }

    private static void listarDoadores() throws SQLException {
        List<Doador> doadores = doadorDAO.listar();
        if (doadores.isEmpty()) {
            System.out.println("Nenhum doador cadastrado.");
        }
        for (Doador d : doadores) {
            System.out.println(d.apresentar());
        }
    }

    private static void atualizarDoador() throws SQLException {
        Doador doador = buscarDoadorOuAvisar(lerInt("ID do doador: "));
        if (doador == null) {
            return;
        }
        System.out.println("Atual: " + doador.apresentar());
        doador.setNome(lerObrigatorio("Novo nome: ", "O nome"));
        doador.setTipoDoador(lerTipoDoador());
        doador.setTelefone(lerObrigatorio("Novo telefone: ", "O telefone"));
        doador.setEmail(lerObrigatorio("Novo e-mail: ", "O e-mail"));
        doadorDAO.atualizar(doador);
        System.out.println("Doador atualizado.");
    }

    private static void removerDoador() throws SQLException {
        int id = lerInt("ID do doador: ");
        if (buscarDoadorOuAvisar(id) == null) {
            return;
        }
        doadorDAO.excluir(id);
        System.out.println("Doador removido.");
    }

    // ---------- Doações ----------

    private static void registrarDoacao() throws SQLException {
        Doador doador = buscarDoadorOuAvisar(lerInt("ID do doador: "));
        if (doador == null) {
            return;
        }
        String tipoAlimento = lerObrigatorio("Tipo de alimento: ", "O tipo de alimento");
        double quantidade = lerQuantidadeKg("Quantidade (kg): ");
        LocalDate validade = lerData("Validade (dd/MM/aaaa): ");
        DoacaoAlimento doacao = new DoacaoAlimento(0, doador.getId(), LocalDate.now(),
                tipoAlimento, quantidade, validade, false);
        doacaoDAO.inserir(doacao);
        System.out.println("Doação registrada: " + doacao);
    }

    private static void historicoDoador() throws SQLException {
        Doador doador = buscarDoadorOuAvisar(lerInt("ID do doador: "));
        if (doador == null) {
            return;
        }
        for (DoacaoAlimento d : doacaoDAO.listarPorDoador(doador.getId())) {
            doador.adicionarDoacao(d);
        }
        System.out.println(doador.apresentar());
        imprimir(doador.getDoacoes());
        System.out.printf("Total doado: %.2f kg%n", doador.getTotalDoadoKg());
    }

    private static void alterarDoacao() throws SQLException {
        DoacaoAlimento doacao = doacaoDAO.buscarPorId(lerInt("ID da doação: "));
        if (doacao == null) {
            System.out.println("Doação não encontrada.");
            return;
        }
        System.out.println("Atual: " + doacao);
        doacao.setTipoAlimento(lerObrigatorio("Novo tipo de alimento: ", "O tipo de alimento"));
        doacao.setQuantidadeKg(lerQuantidadeKg("Nova quantidade (kg): "));
        doacao.setValidade(lerData("Nova validade (dd/MM/aaaa): "));
        doacaoDAO.atualizar(doacao);
        System.out.println("Doação atualizada.");
    }

    private static void distribuirDoacao() throws SQLException {
        DoacaoAlimento doacao = doacaoDAO.buscarPorId(lerInt("ID da doação: "));
        if (doacao == null) {
            System.out.println("Doação não encontrada.");
            return;
        }
        if (!DoacaoAlimento.DISPONIVEL.equals(doacao.getSituacao())) {
            System.out.println("Só é possível distribuir alimentos disponíveis. Situação atual: "
                    + doacao.getSituacao());
            return;
        }
        doacaoDAO.marcarComoDistribuida(doacao.getId());
        System.out.println("Doação marcada como distribuída.");
    }

    // ---------- Auxiliares ----------

    private static Doador buscarDoadorOuAvisar(int id) throws SQLException {
        Doador doador = doadorDAO.buscarPorId(id);
        if (doador == null) {
            System.out.println("Doador não encontrado.");
        }
        return doador;
    }

    private static void imprimir(List<DoacaoAlimento> doacoes) {
        if (doacoes.isEmpty()) {
            System.out.println("Nenhum registro encontrado.");
        }
        for (DoacaoAlimento d : doacoes) {
            System.out.println(d);
        }
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return entrada.nextLine();
    }

    private static String lerTipoDoador() {
        while (true) {
            String resposta = lerTexto("Tipo (1 - Pessoa Física, 2 - Pessoa Jurídica): ").trim();
            if (resposta.equals("1")) {
                return Doador.FISICA;
            }
            if (resposta.equals("2")) {
                return Doador.JURIDICA;
            }
            System.out.println("Atenção: digite 1 ou 2.");
        }
    }

    /** Repete a pergunta até o usuário informar um valor não vazio. */
    private static String lerObrigatorio(String mensagem, String campo) {
        while (true) {
            String valor = lerTexto(mensagem).trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("Atenção: " + campo + " é obrigatório. Tente novamente.");
        }
    }

    /** Repete a pergunta até a quantidade ser maior que zero. */
    private static double lerQuantidadeKg(String mensagem) {
        while (true) {
            double valor = lerDouble(mensagem);
            if (valor > 0) {
                return valor;
            }
            System.out.println("Atenção: a quantidade (kg) deve ser maior que zero.");
        }
    }

    private static int lerInt(String mensagem) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(mensagem).trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            try {
                return Double.parseDouble(lerTexto(mensagem).trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido (ex.: 12,5).");
            }
        }
    }

    private static LocalDate lerData(String mensagem) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(mensagem).trim(), FORMATO);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }
}