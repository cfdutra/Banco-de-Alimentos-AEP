package bancoalimentos.dao;

import bancoalimentos.model.DoacaoAlimento;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD e consultas de doações de alimentos via JDBC. */
public class DoacaoAlimentoDAO {

    private static final String COLUNAS =
            "id, doador_id, data_doacao, tipo_alimento, quantidade_kg, validade, distribuida";

    public void inserir(DoacaoAlimento doacao) throws SQLException {
        String sql = "INSERT INTO doacao_alimento "
                + "(doador_id, data_doacao, tipo_alimento, quantidade_kg, validade, distribuida) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, doacao.getDoadorId());
            ps.setDate(2, Date.valueOf(doacao.getDataDoacao()));
            ps.setString(3, doacao.getTipoAlimento());
            ps.setDouble(4, doacao.getQuantidadeKg());
            ps.setDate(5, Date.valueOf(doacao.getValidade()));
            ps.setBoolean(6, doacao.isDistribuida());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    doacao.setId(chaves.getInt(1));
                }
            }
        }
    }

    public DoacaoAlimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM doacao_alimento WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Histórico de doações de um doador específico. */
    public List<DoacaoAlimento> listarPorDoador(int doadorId) throws SQLException {
        String sql = "SELECT " + COLUNAS
                + " FROM doacao_alimento WHERE doador_id = ? ORDER BY data_doacao DESC, id DESC";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, doadorId);
            return executarLista(ps);
        }
    }

    /** Todos os alimentos ordenados pela validade (mais próximos do vencimento primeiro). */
    public List<DoacaoAlimento> listarPorValidade() throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM doacao_alimento ORDER BY validade ASC, id ASC";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return executarLista(ps);
        }
    }

    /** Alimentos ainda não distribuídos e não vencidos que vencem em até N dias. */
    public List<DoacaoAlimento> listarProximasDoVencimento(int dias) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM doacao_alimento "
                + "WHERE distribuida = FALSE AND validade BETWEEN ? AND ? ORDER BY validade ASC";
        LocalDate hoje = LocalDate.now();
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(hoje));
            ps.setDate(2, Date.valueOf(hoje.plusDays(dias)));
            return executarLista(ps);
        }
    }

    public boolean atualizar(DoacaoAlimento doacao) throws SQLException {
        String sql = "UPDATE doacao_alimento SET data_doacao = ?, tipo_alimento = ?, "
                + "quantidade_kg = ?, validade = ? WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(doacao.getDataDoacao()));
            ps.setString(2, doacao.getTipoAlimento());
            ps.setDouble(3, doacao.getQuantidadeKg());
            ps.setDate(4, Date.valueOf(doacao.getValidade()));
            ps.setInt(5, doacao.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean marcarComoDistribuida(int id) throws SQLException {
        String sql = "UPDATE doacao_alimento SET distribuida = TRUE WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Quantidade total (kg) de alimentos registrada no sistema. */
    public double somarTotalKg() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantidade_kg), 0) FROM doacao_alimento";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private List<DoacaoAlimento> executarLista(PreparedStatement ps) throws SQLException {
        List<DoacaoAlimento> doacoes = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doacoes.add(mapear(rs));
            }
        }
        return doacoes;
    }

    private DoacaoAlimento mapear(ResultSet rs) throws SQLException {
        return new DoacaoAlimento(
                rs.getInt("id"),
                rs.getInt("doador_id"),
                rs.getDate("data_doacao").toLocalDate(),
                rs.getString("tipo_alimento"),
                rs.getDouble("quantidade_kg"),
                rs.getDate("validade").toLocalDate(),
                rs.getBoolean("distribuida"));
    }
}
