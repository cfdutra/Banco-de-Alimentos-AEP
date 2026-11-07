package bancoalimentos.model;


public abstract class Pessoa {

    private String nome;
    private String telefone;

    protected Pessoa(String nome, String telefone) {
        setNome(nome);
        setTelefone(telefone);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            throw new IllegalArgumentException("O telefone é obrigatório.");
        }
        this.telefone = telefone.trim();
    }

    /** Cada tipo de pessoa se apresenta do seu jeito (polimorfismo). */
    public abstract String apresentar();
}
