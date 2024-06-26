package com.zj.httpclient.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class Result {

    private Integer inputNumber;

    private Double squareRes;

    private Long delay;

    private Status status;

    @Override
    public String toString() {
        return "inputNumber=" + inputNumber +
                ", squareRes=" + squareRes +
                ", delay=" + delay +
                ", status=" + status;
    }


}
