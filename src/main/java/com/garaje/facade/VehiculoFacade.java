/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.garaje.facade;

import com.garaje.model.Vehiculo;
import com.garaje.persistence.VehiculoDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Year;
import java.util.Arrays;
import java.util.List;

/**
 * Fachada para operaciones sobre vehículos.
 * Aquí se implementan las reglas de negocio.
 */
public class VehiculoFacade {

    private static final String URL =
            "jdbc:mysql://localhost:3306/taller1_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456";

    /** Lista de colores válidos */
    private static final List<String> COLORES_VALIDOS =
            Arrays.asList("Rojo", "Blanco", "Negro", "Azul", "Gris");

    /** Lista todos los vehículos */
    public List<Vehiculo> listar() throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            VehiculoDAO dao = new VehiculoDAO(con);
            return dao.listar();
        }
    }

    /** Busca vehículo por id */
    public Vehiculo buscarPorId(int id) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            VehiculoDAO dao = new VehiculoDAO(con);
            return dao.buscarPorId(id);
        }
    }

    /** Agrega un vehículo aplicando reglas de negocio */
    public void agregar(Vehiculo v) throws SQLException {
        validarVehiculo(v);

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            VehiculoDAO dao = new VehiculoDAO(con);

            // No permitir placa duplicada
            if (dao.existePlaca(v.getPlaca())) {
                throw new SQLException("La placa ya existe en el sistema");
            }

            dao.agregar(v);

            // Regla Ferrari: notificación simulada
            if ("Ferrari".equalsIgnoreCase(v.getMarca())) {
                System.out.println("⚠ Notificación: Se agregó un Ferrari al sistema");
            }
        }
    }

    /** Actualiza un vehículo aplicando reglas */
    public void actualizar(Vehiculo v) throws SQLException {
        validarVehiculo(v);

        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            VehiculoDAO dao = new VehiculoDAO(con);

            Vehiculo existente = dao.buscarPorId(v.getId());
            if (existente == null) {
                throw new SQLException("No se puede actualizar: vehículo no existe");
            }

            dao.actualizar(v);
        }
    }

    /** Elimina un vehículo, respetando reglas */
    public void eliminar(int id) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {
            VehiculoDAO dao = new VehiculoDAO(con);

            Vehiculo existente = dao.buscarPorId(id);
            if (existente == null) {
                throw new SQLException("No se puede eliminar: vehículo no existe");
            }

            if ("Administrador".equalsIgnoreCase(existente.getPropietario())) {
                throw new SQLException("No se puede eliminar un vehículo con propietario 'Administrador'");
            }

            dao.eliminar(id);
        }
    }

    /** Validaciones generales de un vehículo */
    private void validarVehiculo(Vehiculo v) throws SQLException {
        if (v.getPropietario() == null || v.getPropietario().length() < 5) {
            throw new SQLException("El propietario no puede estar vacío y debe tener al menos 5 caracteres");
        }

        if (v.getMarca() == null || v.getMarca().length() < 3) {
            throw new SQLException("La marca debe tener al menos 3 caracteres");
        }

        if (v.getModelo() == null || v.getModelo().length() < 3) {
            throw new SQLException("El modelo debe tener al menos 3 caracteres");
        }

        if (v.getPlaca() == null || v.getPlaca().length() < 3) {
            throw new SQLException("La placa debe tener al menos 3 caracteres");
        }

        if (!COLORES_VALIDOS.contains(v.getColor())) {
            throw new SQLException("El color debe ser uno de: " + COLORES_VALIDOS);
        }

        try {
            int anioModelo = Integer.parseInt(v.getModelo());
            int anioActual = Year.now().getValue();
            if (anioModelo < anioActual - 20) {
                throw new SQLException("No se permiten vehículos con más de 20 años de antigüedad");
            }
        } catch (NumberFormatException e) {
            throw new SQLException("El modelo debe ser un año numérico válido");
        }

        if (v.getPlaca().contains(";") || v.getMarca().contains("--")) {
            throw new SQLException("Valores inválidos detectados (posible inyección SQL)");
        }
    }
}