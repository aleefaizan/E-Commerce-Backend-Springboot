package com.myecommerceapp.espra.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataChange<T> {

    private ChangeType type;
    private T data;
    public enum ChangeType {
        INSERT,
        UPDATE,
        DELETE
    }
}
