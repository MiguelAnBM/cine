package com.comfenalco.cine.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.comfenalco.cine.dto.response.FuncionResponse;
import com.comfenalco.cine.dto.response.PeliculaResponse;
import com.comfenalco.cine.dto.response.ProductoConfiteriaResponse;
import com.comfenalco.cine.dto.response.SalaResponse;

@SpringBootTest
class ServiceContractTests {

    @Autowired
    private PeliculaService peliculaService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ProductoConfiteriaService productoConfiteriaService;

    @Autowired
    private FuncionService funcionService;

    @Test
    void peliculasSeExponenComoResponse() {
        List<PeliculaResponse> peliculas = peliculaService.listar();

        assertThat(peliculas).hasSize(4);
        assertThat(peliculas).allSatisfy(pelicula -> {
            assertThat(pelicula.id()).isNotNull();
            assertThat(pelicula.titulo()).isNotBlank();
            assertThat(pelicula.duracion()).matches("^[0-9]H [0-5]?[0-9]M$");
            assertThat(pelicula.estado()).isNotNull();
        });

        PeliculaResponse primera = peliculas.get(0);
        assertThat(peliculaService.buscarPorId(primera.id())).isEqualTo(primera);
    }

    @Test
    void salasSeExponenComoResponse() {
        List<SalaResponse> salas = salaService.listar();

        assertThat(salas).hasSize(3);
        assertThat(salas).allSatisfy(sala -> {
            assertThat(sala.id()).isNotNull();
            assertThat(sala.nombre()).isNotBlank();
            assertThat(sala.tipo()).isNotNull();
            assertThat(sala.precioBoleto()).isPositive();
            assertThat(sala.estado()).isNotNull();
        });

        SalaResponse primera = salas.get(0);
        assertThat(salaService.buscarPorId(primera.id())).isEqualTo(primera);
    }

    @Test
    void productosSeExponenComoResponseConCategoria() {
        List<ProductoConfiteriaResponse> productos = productoConfiteriaService.listar();

        assertThat(productos).hasSize(5);
        assertThat(productos).allSatisfy(producto -> {
            assertThat(producto.id()).isNotNull();
            assertThat(producto.nombre()).isNotBlank();
            assertThat(producto.precio()).isPositive();
            assertThat(producto.categoria()).isNotNull();
            assertThat(producto.estado()).isNotNull();
        });

        ProductoConfiteriaResponse primero = productos.get(0);
        assertThat(productoConfiteriaService.buscarPorId(primero.id())).isEqualTo(primero);
    }

    @Test
    void funcionesSeExponenComoResponseConRelacionesYBoletos() {
        Page<FuncionResponse> pagina = funcionService.listar(PageRequest.of(0, 10));

        assertThat(pagina.getTotalElements()).isEqualTo(5);
        assertThat(pagina.getContent()).allSatisfy(funcion -> {
            assertThat(funcion.id()).isNotNull();
            assertThat(funcion.pelicula()).isNotBlank();
            assertThat(funcion.sala()).isNotBlank();
            assertThat(funcion.fecha()).isNotNull();
            assertThat(funcion.horaInicio()).isNotNull();
            assertThat(funcion.horaFin()).isNotNull();
            assertThat(funcion.estado()).isNotNull();
            assertThat(funcion.boletosVendidos()).isNotNegative();
        });
        assertThat(pagina.getContent())
                .anySatisfy(funcion -> assertThat(funcion.boletosVendidos()).isEqualTo(2));
    }

    @Test
    void peliculasEnCarteleraSeExponenComoResponse() {
        List<PeliculaResponse> peliculas = funcionService.peliculasEnCartelera();

        assertThat(peliculas).hasSize(4);
        assertThat(peliculas).allSatisfy(pelicula -> {
            assertThat(pelicula.id()).isNotNull();
            assertThat(pelicula.titulo()).isNotBlank();
        });
    }
}
