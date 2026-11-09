package com.comfenalco.cine.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.comfenalco.cine.model.DetalleBoleto;
import com.comfenalco.cine.model.DetalleConfiteria;
import com.comfenalco.cine.model.Factura;
import com.comfenalco.cine.model.Funcion;
import com.comfenalco.cine.model.Pelicula;
import com.comfenalco.cine.model.ProductoConfiteria;
import com.comfenalco.cine.model.Sala;
import com.comfenalco.cine.model.Usuario;
import com.comfenalco.cine.model.enums.CategoriaProducto;
import com.comfenalco.cine.model.enums.Clasificacion;
import com.comfenalco.cine.model.enums.Genero;
import com.comfenalco.cine.model.enums.MetodoPago;
import com.comfenalco.cine.model.enums.RolUsuario;
import com.comfenalco.cine.model.enums.TipoSala;
import com.comfenalco.cine.repository.FacturaRepository;
import com.comfenalco.cine.repository.FuncionRepository;
import com.comfenalco.cine.repository.PeliculaRepository;
import com.comfenalco.cine.repository.ProductoConfiteriaRepository;
import com.comfenalco.cine.repository.SalaRepository;
import com.comfenalco.cine.repository.UsuarioRepository;

// =====================================================================
// QUÉ ES:   los datos de ejemplo: 2 usuarios, 4 películas, 3 salas,
//           5 funciones, 5 productos de confitería y 2 facturas.
// POR QUÉ:  la base de datos es en memoria (H2) y se borra al parar.
//           Sin esto, la aplicación arrancaría vacía.
// CÓMO:     CommandLineRunner = "Spring, ejecuta run() justo después de
//           arrancar". @Component para que Spring la encuentre.
// =====================================================================
@Component
public class DatosIniciales implements CommandLineRunner {

        private final UsuarioRepository usuarioRepository;
        private final PeliculaRepository peliculaRepository;
        private final SalaRepository salaRepository;
        private final FuncionRepository funcionRepository;
        private final ProductoConfiteriaRepository productoConfiteriaRepository;
        private final FacturaRepository facturaRepository;
        private final PasswordEncoder passwordEncoder;

        public DatosIniciales(UsuarioRepository usuarioRepository,
                PeliculaRepository peliculaRepository,
                SalaRepository salaRepository,
                FuncionRepository funcionRepository,
                ProductoConfiteriaRepository productoConfiteriaRepository,
                FacturaRepository facturaRepository,
                PasswordEncoder passwordEncoder) {
                this.usuarioRepository = usuarioRepository;
                this.peliculaRepository = peliculaRepository;
                this.salaRepository = salaRepository;
                this.funcionRepository = funcionRepository;
                this.productoConfiteriaRepository = productoConfiteriaRepository;
                this.facturaRepository = facturaRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        @Transactional
        public void run(String... args) {
                if (usuarioRepository.count() > 0) {
                        return;                       // ya hay datos: no duplicar
                }

                List<Usuario> usuarios = sembrarUsuarios();
                List<Pelicula> peliculas = sembrarPeliculas();
                List<Sala> salas = sembrarSalas();
                List<Funcion> funciones = sembrarFunciones(peliculas, salas);
                List<ProductoConfiteria> productos = sembrarProductos();

                sembrarFacturas(usuarios, funciones, productos);
        }

        private List<Usuario> sembrarUsuarios() {
                Usuario administrador = new Usuario(
                        "Administrador General",
                        "admin",
                        RolUsuario.ADMINISTRADOR,
                        passwordEncoder.encode("admin123"));

                Usuario taquillero = new Usuario(
                        "Laura Taquilla",
                        "laura",
                        RolUsuario.TAQUILLERO,
                        null);

                return usuarioRepository.saveAll(List.of(administrador, taquillero));
        }

        private List<Pelicula> sembrarPeliculas() {
                Pelicula dune = new Pelicula(
                        "Dune: Parte Dos",
                        "Paul Atreides se une a los Fremen para vengar a su familia.",
                        "2H 46M",
                        Clasificacion.MAYORES_15,
                        Genero.CIENCIA_FICCION,
                        "https://example.com/dune-parte-dos.jpg");

                Pelicula interestelar = new Pelicula(
                        "Interestelar",
                        "Un grupo de exploradores busca un nuevo hogar para la humanidad.",
                        "2H 49M",
                        Clasificacion.MAYORES_7,
                        Genero.CIENCIA_FICCION,
                        "https://example.com/interestelar.jpg");

                Pelicula coco = new Pelicula(
                        "Coco",
                        "Miguel viaja al mundo de los muertos para descubrir su historia.",
                        "1H 45M",
                        Clasificacion.TODO_PUBLICO,
                        Genero.ANIMACION,
                        "https://example.com/coco.jpg");

                Pelicula padrino = new Pelicula(
                        "El padrino",
                        "La historia de la familia Corleone y su control del crimen.",
                        "2H 55M",
                        Clasificacion.MAYORES_15,
                        Genero.DRAMA,
                        "https://example.com/el-padrino.jpg");

                return peliculaRepository.saveAll(List.of(dune, interestelar, coco, padrino));
        }

        private List<Sala> sembrarSalas() {
                Sala salaUno = new Sala(
                        "Sala 1",
                        TipoSala.DOS_D,
                        new BigDecimal("18000.00"));

                Sala salaDos = new Sala(
                        "Sala 2",
                        TipoSala.TRES_D,
                        new BigDecimal("22000.00"));

                Sala salaImax = new Sala(
                        "Sala IMAX",
                        TipoSala.IMAX,
                        new BigDecimal("30000.00"));

                return salaRepository.saveAll(List.of(salaUno, salaDos, salaImax));
        }

        private List<Funcion> sembrarFunciones(List<Pelicula> peliculas, List<Sala> salas) {
                Pelicula dune = peliculas.get(0);
                Pelicula interestelar = peliculas.get(1);
                Pelicula coco = peliculas.get(2);
                Pelicula padrino = peliculas.get(3);

                Sala salaUno = salas.get(0);
                Sala salaDos = salas.get(1);
                Sala salaImax = salas.get(2);

                LocalDate manana = LocalDate.now().plusDays(1);

                Funcion duneTarde = new Funcion(
                        dune,
                        salaUno,
                        manana,
                        LocalTime.of(18, 0));

                Funcion duneNoche = new Funcion(
                        dune,
                        salaDos,
                        manana,
                        LocalTime.of(20, 30));

                Funcion interestelarTarde = new Funcion(
                        interestelar,
                        salaUno,
                        manana.plusDays(1),
                        LocalTime.of(19, 0));

                Funcion cocoManana = new Funcion(
                        coco,
                        salaImax,
                        manana.plusDays(1),
                        LocalTime.of(16, 0));

                Funcion padrinoNoche = new Funcion(
                        padrino,
                        salaDos,
                        manana.plusDays(2),
                        LocalTime.of(21, 0));

                return funcionRepository.saveAll(List.of(duneTarde,
                        duneNoche, interestelarTarde, cocoManana, padrinoNoche));
        }

        private List<ProductoConfiteria> sembrarProductos() {
                ProductoConfiteria crispetasGrandes = new ProductoConfiteria(
                        "Crispetas grandes",
                        "Crispetas con mantequilla",
                        new BigDecimal("12000.00"),
                        null,
                        CategoriaProducto.SNACKS);

                ProductoConfiteria crispetasMedianas = new ProductoConfiteria(
                        "Crispetas medianas",
                        "Crispetas con mantequilla",
                        new BigDecimal("9000.00"),
                        null,
                        CategoriaProducto.SNACKS);

                ProductoConfiteria gaseosa = new ProductoConfiteria(
                        "Gaseosa",
                        "Bebida fría de 500 ml",
                        new BigDecimal("6000.00"),
                        null,
                        CategoriaProducto.BEBIDAS);

                ProductoConfiteria perroCaliente = new ProductoConfiteria(
                        "Perro caliente",
                        "Perro caliente con papas",
                        new BigDecimal("10000.00"),
                        null,
                        CategoriaProducto.SNACKS);

                ProductoConfiteria chocolatina = new ProductoConfiteria(
                        "Chocolatina",
                        "Chocolatina de leche",
                        new BigDecimal("4500.00"),
                        null,
                        CategoriaProducto.DULCES);

                return productoConfiteriaRepository.saveAll(List.of(crispetasGrandes,
                        crispetasMedianas, gaseosa, perroCaliente, chocolatina));
        }

        private void sembrarFacturas(List<Usuario> usuarios, List<Funcion> funciones,
                List<ProductoConfiteria> productos) {
                Usuario administrador = usuarios.get(0);
                Usuario taquillero = usuarios.get(1);

                Funcion duneTarde = funciones.get(0);
                Funcion interestelarTarde = funciones.get(2);

                ProductoConfiteria crispetasGrandes = productos.get(0);
                ProductoConfiteria gaseosa = productos.get(2);
                ProductoConfiteria perroCaliente = productos.get(3);

                Factura facturaMostrador = new Factura(
                        taquillero,
                        MetodoPago.EFECTIVO,
                        "Cliente de mostrador",
                        "mostrador@example.com");

                facturaMostrador.agregarBoleto(new DetalleBoleto(duneTarde, "A1"));
                facturaMostrador.agregarBoleto(new DetalleBoleto(duneTarde, "A2"));
                facturaMostrador.agregarProducto(new DetalleConfiteria(crispetasGrandes, 1));
                facturaMostrador.agregarProducto(new DetalleConfiteria(gaseosa, 2));

                facturaRepository.save(facturaMostrador);

                Factura facturaWeb = new Factura(
                        administrador,
                        MetodoPago.TARJETA,
                        "Cliente web",
                        "web@example.com");

                facturaWeb.agregarBoleto(new DetalleBoleto(interestelarTarde, "B1"));
                facturaWeb.agregarProducto(new DetalleConfiteria(perroCaliente, 1));

                facturaRepository.save(facturaWeb);
        }
}
