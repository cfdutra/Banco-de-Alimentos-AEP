package bancoalimentos.dao;

import bancoalimentos.model.Doador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** CRUD de doadores via JDBC. */
public class DoadorDAO {

    public void inserir(Doador doador) throws SQLException {
        String sql = "INSERT INTO doador (nome, tipo_doador, telefone, email) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doador.getNome());
            ps.setString(2, doador.getTipoDoador());
            ps.setString(3, doador.getTelefone());
            ps.setString(4, doador.getEmail());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    doador.setId(chaves.getInt(1));
                }
            }
        }
    }

    public List<Doador> listar() throws SQLException {
        String sql = "SELECT id, nome, tipo_doador, telefone, email FROM doador ORDER BY nome";
        List<Doador> doadores = new ArrayList<>();
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doadores.add(mapear(rs));
            }
        }
        return doadores;
    }

    /** Retorna o doador ou null se não existir. */
    public Doador buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, tipo_doador, telefone, email FROM doador WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean atualizar(Doador doador) throws SQLException {
        String sql = "UPDATE doador SET nome = ?, tipo_doador = ?, telefone = ?, email = ? WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doador.getNome());
            ps.setString(2, doador.getTipoDoador());
            ps.setString(3, doador.getTelefone());
            ps.setString(4, doador.getEmail());
            ps.setInt(5, doador.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Regra de negócio: um doador com doações vinculadas não pode ser removido,
     * para preservar o histórico de arrecadação.
     */
    public boolean excluir(int id) throws SQLException {
        if (possuiDoacoes(id)) {
            throw new IllegalStateException(
                    "Este doador possui doações vinculadas e não pode ser removido.");
        }
        String sql = "DELETE FROM doador WHERE id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean possuiDoacoes(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM doacao_alimento WHERE doador_id = ?";
        try (Connection con = ConexaoDB.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Doador mapear(ResultSet rs) throws SQLException {
        return new Doador(rs.getInt("id"), rs.getString("nome"), rs.getString("tipo_doador"),
                rs.getString("telefone"), rs.getString("email"));
    }
}
