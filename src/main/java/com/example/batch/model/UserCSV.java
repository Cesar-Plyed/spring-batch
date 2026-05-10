package com.example.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCSV {
    private String name;
    private String lastname;
    private String email;
    private Integer age;

    @Override
    public String toString() {
        return String.format(
                "UsuarioCSV{nombre='%s', apellido='%s', email='%s', edad=%d}", 
                name,
                lastname,
                email,
                age
        );
    }
}
