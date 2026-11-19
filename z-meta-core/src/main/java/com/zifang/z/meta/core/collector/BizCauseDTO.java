package com.zifang.z.meta.core.collector;

/**
 * 因果链元数据：一条「因为 / 条件 / 导致」的边。
 *
 * @author zifang
 * @since 1.1.0
 */
public class BizCauseDTO {

    /** 主体：谁导致。 */
    private String subject;

    /** 效果：导致什么。 */
    private String effect;

    /** 原因：是什么促成了该效果。 */
    private String because;

    /** 前提条件。 */
    private String condition;

    /** 权重（0~1）。 */
    private double weight = 1.0;

    /** 声明来源：类名#方法名。 */
    private String source;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getBecause() {
        return because;
    }

    public void setBecause(String because) {
        this.because = because;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
