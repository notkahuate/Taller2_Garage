/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.garaje.controller;

import com.garaje.model.Vehiculo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/vehiculos")
public class VehiculoServlet extends HttpServlet {

    private static final String URL =
        "jdbc:mysql://localhost:3306/taller1_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "123456";

  @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    String action = req.getParameter("action");

    if ("editar".equals(action)) {
        String idStr = req.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, placa, marca, modelo, color, propietario FROM vehiculo WHERE id=?")) {

                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Vehiculo v = new Vehiculo(
                            rs.getInt("id"),
                            rs.getString("placa"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("color"),
                            rs.getString("propietario")
                        );
                        req.setAttribute("vehiculo", v);
                    }
                }
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        req.getRequestDispatcher("vehiculo-form.jsp").forward(req, resp);
        return;
    }

    if ("eliminar".equals(action)) {
        String idStr = req.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM vehiculo WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        }
        resp.sendRedirect("vehiculos");
        return;
    }

    // Listar vehículos si no hay acción
    List<Vehiculo> vehiculos = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT id, placa, marca, modelo, color, propietario FROM vehiculo")) {

        while (rs.next()) {
            Vehiculo v = new Vehiculo(
                rs.getInt("id"),
                rs.getString("placa"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getString("color"),
                rs.getString("propietario")
            );
            vehiculos.add(v);
        }

    } catch (SQLException e) {
        throw new ServletException(e);
    }

    req.setAttribute("vehiculos", vehiculos);
    req.getRequestDispatcher("vehiculos.jsp").forward(req, resp);
}


    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    String action = req.getParameter("action");

    String placa = req.getParameter("placa");
    String marca = req.getParameter("marca");
    String modelo = req.getParameter("modelo");
    String color = req.getParameter("color");
    String propietario = req.getParameter("propietario");

    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

        if ("insertar".equals(action)) {
            String sql = "INSERT INTO vehiculo (placa, marca, modelo, color, propietario) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, placa);
                ps.setString(2, marca);
                ps.setString(3, modelo);
                ps.setString(4, color);
                ps.setString(5, propietario);
                ps.executeUpdate();
            }
        } else if ("actualizar".equals(action)) {
            String idStr = req.getParameter("id");
            if (idStr != null && !idStr.isEmpty()) {
                int id = Integer.parseInt(idStr);
                String sql = "UPDATE vehiculo SET placa=?, marca=?, modelo=?, color=?, propietario=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, placa);
                    ps.setString(2, marca);
                    ps.setString(3, modelo);
                    ps.setString(4, color);
                    ps.setString(5, propietario);
                    ps.setInt(6, id);
                    ps.executeUpdate();
                }
            }
        }

    } catch (SQLException e) {
        throw new ServletException(e);
    }

    resp.sendRedirect("vehiculos");
}
}