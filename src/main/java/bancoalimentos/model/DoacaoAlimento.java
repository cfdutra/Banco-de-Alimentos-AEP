package bancoalimentos.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DoacaoAlimento {

    public static final int DIAS_ALERTA_VENCIMENTO = 7;
    public static final String DISPONIVEL = "DISPONIVEL";
    public static final String DISTRIBUIDO = "DISTRIBUIDO";
    public static final String VENCIDO = "VENCIDO";

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private int doadorId;
    private LocalDate dataDoacao;
    private String tipoAlimento;
    private double quantidadeKg;
    private LocalDate validade;
    private boolean distribuida;

    public DoacaoAlimento(int id, int doadorId, LocalDate dataDoacao, String tipoAlimento,
                          double quantidadeKg, LocalDate validade, boolean distribuida) {
        this.id = id;
        this.doadorId = doadorId;
        this.distribuida = distribuida;
        setDataDoacao(dataDoacao);
        setTipoAlimento(tipoAlimento);
        setQuantidadeKg(quantidadeKg);
        setValidade(validade);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDoadorId() {
        return doadorId;
    }

    public LocalDate getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDate dataDoacao) {
        if (dataDoacao == null) {
            throw new IllegalArgumentException("A data da doação é obrigatória.");
        }
        this.dataDoacao = dataDoacao;
    }

    public String getTipoAlimento() {
        return tipoAlimento;
    }

    public void setTipoAlimento(String tipoAlimento) {
        if (tipoAlimento == null || tipoAlimento.isBlank()) {
            throw new IllegalArgumentException("O tipo de alimento é obrigatório.");
        }
        this.tipoAlimento = tipoAlimento.trim();
    }

    public double getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(double quantidadeKg) {
        if (quantidadeKg <= 0) {
            throw new IllegalArgumentException("A quantidade (kg) deve ser maior que zero.");
        }
        this.quantidadeKg = quantidadeKg;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        if (validade == null) {
            throw new IllegalArgumentException("A data de validade é obrigatória.");
        }
        this.validade = validade;
    }

    public boolean isDistribuida() {
        return distribuida;
    }

    public void setDistribuida(boolean distribuida) {
        this.distribuida = distribuida;
    }

    /** Situação do alimento: DISTRIBUIDO, VENCIDO ou DISPONIVEL. */
    public String getSituacao() {
        if (distribuida) {
            return DISTRIBUIDO;
        }
        if (validade.isBefore(LocalDate.now())) {
            return VENCIDO;
        }
        return DISPONIVEL;
    }

    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), validade);
    }

    /** Alimento disponível a até 7 dias do vencimento tem prioridade de distribuição. */
    public boolean isPrioritaria() {
        return DISPONIVEL.equals(getSituacao()) && getDiasParaVencer() <= DIAS_ALERTA_VENCIMENTO;
    }

    @Override
    public String toString() {
        String prioridade = isPrioritaria() ? " [PRIORIDADE - vence em " + getDiasParaVencer() + " dia(s)]" : "";
        return "Doação #" + id + " | Doador " + doadorId + " | " + tipoAlimento
                + " | " + String.format("%.2f", quantidadeKg) + " kg"
                + " | Doada em " + dataDoacao.format(FORMATO)
                + " | Validade " + validade.format(FORMATO)
                + " | " + getSituacao() + prioridade;
    }
}
