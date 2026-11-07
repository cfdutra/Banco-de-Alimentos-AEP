package bancoalimentos.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Doador extends Pessoa {

    public static final String FISICA = "FISICA";
    public static final String JURIDICA = "JURIDICA";

    private int id;
    private String tipoDoador;
    private String email;
    private final List<DoacaoAlimento> doacoes = new ArrayList<>();

    public Doador(int id, String nome, String tipoDoador, String telefone, String email) {
        super(nome, telefone);
        this.id = id;
        setTipoDoador(tipoDoador);
        setEmail(email);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoDoador() {
        return tipoDoador;
    }

    public void setTipoDoador(String tipoDoador) {
        if (tipoDoador == null
                || !(tipoDoador.equals(FISICA) || tipoDoador.equals(JURIDICA))) {
            throw new IllegalArgumentException("O tipo do doador deve ser FISICA ou JURIDICA.");
        }
        this.tipoDoador = tipoDoador;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }
        this.email = email.trim();
    }

    public List<DoacaoAlimento> getDoacoes() {
        return Collections.unmodifiableList(doacoes);
    }

    public void adicionarDoacao(DoacaoAlimento doacao) {
        doacoes.add(doacao);
    }

    public double getTotalDoadoKg() {
        double total = 0;
        for (DoacaoAlimento d : doacoes) {
            total += d.getQuantidadeKg();
        }
        return total;
    }

    @Override
    public String apresentar() {
        String tipo = FISICA.equals(tipoDoador) ? "Pessoa Física" : "Pessoa Jurídica";
        return "[" + id + "] " + getNome() + " (" + tipo + ") - Tel: "
                + getTelefone() + " - E-mail: " + email;
    }
}
