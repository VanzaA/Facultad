Ejercicio 1
AGENCIA (RAZON_SOCIAL, dirección, telef, e-mail)
CIUDAD (CODIGOPOSTAL, nombreCiudad, añoCreación)
CLIENTE (DNI, nombre, apellido, teléfono, dirección)
VIAJE( FECHA,HORA,DNI, cpOrigen, cpDestino, razon_social, descripcion) //cpOrigen y
cpDestino corresponden a la ciudades origen y destino del viaje

1. Reportar nombre, apellido, dirección y teléfono de clientes con más de 5 viajes.

select c.nombre, c.apellido, c.direccion, c.telef
from cliente c inner join viaje v on (c.dni=v.dni)
group by c.dni,c.nombre, c.apellido, c.direccion, c.telef
having count(*)>5

2. Listar razón social, dirección y teléfono de agencias que realizaron viajes a la ciudad de
‘Lincoln’ (ciudad destino) y que el cliente sea ‘Juan Perez’. Ordenar por dirección y luego
por teléfono.
select distinct (a.razon_social), a.direccion, a.telef
from viaje v inner join cliente c on (v.dni = c.dni) 
inner join agencia a on(a.razon_social=v.razon_social) inner join ciudad ci on(v.cpdestino=ci.codigopostal) 
where c.nombre="juan" AND c.apellido="perez" AND ci.nombreCiudad="lincoln"
order by a.direccion, a.telef

3. Listar fecha, hora, datos personales del cliente, ciudad origen y destino de viajes realizados
en octubre de 2017.

select v.fecha, v.hora, c.*,cd.nombreCiudad, co.nombreCiudad
from viaje v inner join cliente c on (v.dni = c.dni) inner join ciudad cd on(v.cpdestino=cd.codigopostal) 
inner join ciudad co on (v.cporigen=co.codigopostal)
where month(v.fecha)=10 and year(v.fecha)=2017

4. Informar cantidad de viajes de la agencia con razón social ‘Y’.

select count(*)
from viaje
where razon_social="Y"

5. Borrar la agencia con razón social: ‘RemisesX’.

Delete from viaje 
where razon_social="RemisesX"
Delete from agencia
where razon_social="RemisesX"

6. Listar datos personales de clientes que viajaron con ciudad destino ‘Junín’ pero no viajaron
con ciudad destino ‘La Plata’.
SELECT DISTINCT(c.*)
FROM Cliente C 
WHERE C.DNI IN (
	SELECT V.DNI
	FROM viaje v INNER JOIN ciudad c ON(V.cpDestino=v.codigopostal)
	WHERE C.nombreCiudad = "junin")
AND C.DNI NOT IN (
	SELECT V.DNI
	FROM viaje v INNER JOIN ciudad c ON(V.cpDestino=v.codigopostal)
	WHERE C.nombreCiudad = "La plata")

7. Listar nombre, apellido, dirección y teléfono de clientes que viajaron con todas las agencias.
select c.nombre, c.apellido, c.direccion, c.telefono
from cliente c
where not exists(select * from agencia a where not exists(select * from viaje v where v.dni=c.dni and v.razon_social=a.razon_social))

8. Reportar información de agencias que realizaron viajes durante 2017 o que tengan dirección
igual a ‘Gorina’.
select a.*
from viaje v inner join agencia a on(a.razon_social=v.razon_social)
where a.direccion="Gorina" or YEAR(v.fecha)=2017

9. Actualizar el teléfono del cliente con DNI: 28329665 a: 221-4400897.
update cliente
set telefono=2214400897
where dni=28329665

Ejercicio 2
Cliente(idCliente, nombre, apellido, DNI, telefono, direccion)
Factura (nroTicket, total, fecha, hora,idCliente)
Detalle(nroTicket, idProducto, cantidad, preciounitario)
Producto(idProducto, descripcion, precio, nombreP, stock)

1)1. Listar DNI, apellido y nombre de clientes donde el monto total comprado, teniendo en
cuenta todas sus facturas, supere $2.000.000.

select c.nombre, c.apellido, c.dni
from cliente c inner join factura f on (f.idCliente=c.idCliente)
group by  c.idCliente, c.nombre, c.apellido, c.dni
HAVING sum(f.total)>2000000

2) Listar nombre, descripción, precio y stock de productos no vendidos. Ordenar por
nombre.
select p.nombre,p.descripción, p.precio, p.stock
from Producto p
where not exists(select * from detalle d where p.idProducto=d.idProducto)

3. Listar para cada producto: nombre y cuantas veces fué vendido. Tenga en cuenta que
puede no haberse vendido nunca el producto. 

select p.nombreP, count(*)
from producto p inner join detalle d on(p.idProducto=d.idProducto)
group by p.idProducto, p.nombreP
union
select p.nombreP, 0
from producto p 
where not exists(select *  from detalle d where p.idProducto= d.idProducto)

select p.nombreP, count(d.idProducto)
from producto p left join detalle d on(p.idProducto=d.idProducto)
group by p.idProducto, p.nombreP

4. Listar nombre, apellido, DNI, teléfono y dirección de clientes que compraron el producto
con nombre ‘Z’ y nunca compraron el producto con nombre ‘J’.

select distinct(c.dni), c.nombre, c.apellido, c.telefono, c.dirección 
from cliente c
where c.idCliente in(
	select f.idCliente
	from factura f inner join detalle d on(f.nroTicket = D.nroTicket) inner join producto p on (d.idProducto=p.idProducto)
	where p.nombreP="z"
) and c.idCliente not in(
	select f.idCliente
	from factura f inner join detalle d on(f.nroTicket = D.nroTicket) inner join producto p on (d.idProducto=p.idProducto)
	where p.nombreP="j"
)

5. Listar nroTicket, total, fecha, hora y DNI del cliente, de aquellas facturas donde se haya
comprado el producto ‘w’.

select  f.nroTicket, f.total,f.fecha, f.hora, c.dni
from cliente c inner join factura f on(c.idCliente=f.idCliente) 
inner join detalle d on(f.nroTicket = D.nroTicket) inner join producto p on (d.idProducto=p.idProducto)
where p.nombreP="w"

6. Agregar un producto con id de producto 1212, descripción “tornillo para marco de metal”,
precio $12, nombreP “tornillo 2mm” y stock 22. Se supone que el idProducto 1212 no
existe.
insert into producto values (1212, "tornillo para marco de metal",12,"tornillo 2mm",22)

7. Listar nombre, apellido, DNI, teléfono y dirección de clientes que realizaron compras
durante 2017.

SELECT DISTINCT(c.DNI) c.nombre, c.apellido, c.telefono, c.dirección
FROM cliente c INNER JOIN factura f ON f.idCliente = c.idCliente
where YEAR(f.fecha) = 2017

8. Listar nombre, descripción, precio y stock de productos vendidos al cliente con
DNI:25866239 pero que no fueron vendidos al cliente con DNI 2589633.

SELECT p.nombreP, p.descripcion, p.precio, p.stock
FROM Producto
WHERE p.idProducto IN(
	SELECT f.idProducto 
	FROM factura f INNER JOIN destalle d ON d.nroTicket = f.nroTicket
	INNER JOIN cliente c ON c.idCliente = f.idCliente
	WHERE c.idCliente = 25866239
) AND p.idProducto NOT IN(
	SELECT f.idProducto 
	FROM factura f INNER JOIN destalle d ON d.nroTicket = f.nroTicket
	INNER JOIN cliente c ON c.idCliente = f.idCliente
	WHERE c.idCliente = 2589633
)

9. Listar nroTicket, total, fecha, hora para las facturas del cliente ´Jorge Perez´ donde haya
comprado el producto ´Z´.

SELECT f.nroTicket, f.total, f.fecha, f.hora
FROM factura f 
INNER JOIN cliente c ON f.idCliente = c.idCliente
INNER JOIN detalle d ON d.nroTicket = f.nroTicket
INNER JOIN producto p ON p.idProducto = d.idProducto
WHERE c.nombre = "Jorge" AND c.apellido = "Perez" AND p.nombreP = "Z"

Ejercicio 3:
SOCIO = (Cod_Socio, DNI, Apellido, Nombre, Fecha_Nacimiento, Fecha_Ingreso)
LIBRO = (ISBN, Titulo, Cod_Genero, Descripcion)
COPIA = (ISBN, Nro_Ejemplar, Estado)
EDITORIAL = (Cod_Editorial, Denominacion, Telefono, Calle, Numero, Piso, Dpto.)
LIBRO-EDITORIAL = (ISBN, Cod_Editorial, Año_Edicion)
GENERO = (Cod_Genero, Nombre)
PRESTAMO = (Nro_Prestamo, Cod_Socio, ISBN, Nro_Ejemplar, Fecha_Prestamo,
Fecha_Devolucion)

1. Listar DNI, apellido, nombre y fecha de ingreso de todos los socios de la
biblioteca.
SELECT dni, apellido, nombre, Fecha_Ingreso
FROM socio 

2. Listar apellido, nombre y fecha de nacimiento de los socios cuya fecha de ingreso
sea mayor 10/01/2017 y se apelliden ‘Lopez’.
SELECT apellido, nombre, Fecha_Nacimiento
FROM socio
WHERE date(Fecha_Ingreso) > 10/01/2017 and apellido=‘Lopez’

3. Listar ISBN, título y descripción de todos los libros cuyo género sea “Drama”.
SELECT l.ISBN, l.titulo, l.descripcion
FROM LIBRO l INNER JOIN GENERO g ON (l.Cod_Genero=g.Cod_Genero)
WHERE g.nombre=“Drama”

4. Listar ISBN, título y descripción de todos los libros cuyo estado de la copia sea
“Regular”.

SELECT l.ISBN, l.titulo, l.descripcion
FROM libro l 
INNER JOIN copia c ON l.ISBN = c.ISBN
WHERE c.estado = "Regular"

5. Listar apellido, nombre y fecha de nacimiento de los socios que solo pidieron
préstamos de libros con género ‘Novela’.
SELECT s.apellido, s.nombre, s.Fecha_Nacimiento,
FROM socio s 
INNER JOIN prestamo p ON s.Cod_Socio = p.Cod_Socio
INNER JOIN libro l ON l.ISBN=p.ISBN
INNER JOIN Genero g ON g.Cod_Genero=L.Cod_Genero
WHERE g.nombre = "Novela" AND not exists(
	SELECT * 
	FROM socio ss 
	INNER JOIN prestamo pp ON ss.Cod_Socio = pp.Cod_Socio
	INNER JOIN libro ll ON ll.ISBN=pp.ISBN
	INNER JOIN Genero gg ON gg.Cod_Genero=ll.Cod_Genero
	WHERE gg.nombre <> "Novela"AND s.Cod_Socio=ss.Cod_Socio)

6. Listar el Apellido y Nombre de aquellos socios cuya fecha de ingreso este entre el
01/01/2017 y el 30/09/2017. Dicho listado deberá estar ordenado por Apellido y
Nombre.

SELECT s.nombre, s.apellido
FROM socio s 
WHERE s.Fecha_Ingreso BETWEEN DATE(01/01/2017) AND DATE(30/09/2017)
ORDER BY s.apellido, s.nombre

7. Listar el Titulo, Género (el Nombre del Género) y Descripción de aquellos libros
editados por la editorial “X” durante 2017. Dicho listado deberá estar ordenado por
Título.

SELECT l.titulo, g.Nombre, l.descripcion
FROM libro l
INNER JOIN LIBRO-EDITORIAL le ON le.ISBN = l.ISBN
INNER JOIN editorial e ON e.Cod_Editorial = le.Cod_Editorial
INNER JOIN GENERO g ON g.Cod_Genero = l.Cod_Genero
WHERE e.Denominacion = 'X' 
	AND YEAR(le.Año_Edicion) = 2017
ORDER BY l.titulo

8. Listar el Apellido, Nombre, Fecha de Nacimiento y cantidad de préstamos de
aquellos socios que tengan menos de 15 préstamos. Dicho listado deberá estar
ordenado por cantidad de préstamos y luego por apellido.

SELECT  s.apellido, s.nombre, s.Fecha_Nacimiento, count(*) AS prestamos
FROM SOCIO S INNER JOIN PRESTAMO P ON(S.Cod_Socio=P.Cod_Socio)
GROUP BY s.apellido, s.nombre, s.Fecha_Nacimiento 
HAVING COUNT(*)< 15
ORDER BY prestamos, apellido

10. Proyectar que cantidad de socios tienen actualmente libros prestados cuyo estado
sea “Muy Bueno”.

SELECT COUNT(*)
FROM  PRESTAMO P INNER JOIN copia c ON (c.ISBN=p.ISBN AND c.Nro_Ejemplar=p.Nro_Ejemplar)
WHERE c.Estado="muy bueno" AND p.Fecha_Devolucion > NOW() 
GROUP BY p.Cod_Socio


11. Listar el Título, Género, Denominación de la editorial y Año de edición de aquellos
libros editados entre los años 2010 y 2017 qué no hayan sido prestados nunca. 
Dicho listado deberá estar ordenado por año de edición y título del libro.

SELECT  l.titulo, g.nombre, e.Denominación, le.Año_Edicion
FROM editorial e INNER JOIN LIBRO-EDITORIAL le ON(e.Cod_Editorial=le.Cod_Editorial)
INNER JOIN LIBRO l ON(L.ISBN=le.ISBN) INNER JOIN GENERO g on(g.Cod_Genero=l.Cod_Genero)
WHERE (le.Año_Edicion BETWEEN 2010 AND 2017) and l.isbn NOT IN(
	select li.isbn
	FROM libro li INNER JOIN prestamos p ON(li.ISBN=p.ISBN))
ORDER BY le.Año_Edicion, l.titulo

12. Agregar un nuevo socio con el DNI, Apellido, Nombre y Fecha de nacimiento que
prefiera.

INSERT INTO socio(DNI, Apellido, Nombre, Fecha_Nacimiento) values (11010010, 'Que tierni', 'Puto el', 1/1/-1123)

13. Modificar la descripción del libro cuyo ISBN es 1235_4554 por: ‘Una novela de
intriga’.

UPDATE LIBRO
SET descripcion = "Una novela de intriga"
WHERE ISBN = "1235_4554"


14. Listar para cada Editorial la cantidad de copias prestadas. Dicho listado deberá
estar ordenado en forma descendente por cantidad de copias y nombre de la
editorial.

SELECT e.Denominación, COUNT(*) as cantidad
FROM editorial e
INNER JOIN LIBRO-EDITORIAL le ON(l.Cod_Editorial = le.Cod_Editorial)
INNER JOIN prestamo p ON (le.ISBN = p.ISBN)
GROUP BY e.Cod_Editorial
ORDER BY DESC, cantidad, e.Denominación

Ejercicio 4:
PERSONA = (DNI, Apellido, Nombre, Fecha_Nacimiento, Estado_Civil, Genero)
ALUMNO = (DNI, Legajo, Año_Ingreso)
PROFESOR = (DNI, Matricula, Nro_Expediente)
TITULO = (Cod_Titulo, Nombre, Descripción)
TITULO-PROFESOR = (Cod_Titulo, DNI, Fecha)
CURSO = (Cod_Curso, Nombre, Descripción, Fecha_Creacion, Duracion)
ALUMNO-CURSO = (DNI, Cod_Curso, Año, Desempeño, Calificación)
PROFESOR-CURSO = (DNI, Cod_Curso, Fecha_Desde, Fecha_Hasta)

7. Listar el DNI, Apellido, Nombre, Cantidad de horas y Promedio de horas que dicta cada
profesor. La cantidad de horas se calcula como la suma de la duración de todos los
cursos que dicta.

SELECT P.DNI, P.Apellido, p.nombre, SUM(c.duración), AVG(c.duración)
FROM PERSONA p 
INNER JOIN PROFESOR-CURSO pc ON pc.DNI = t.DNI
INNER JOIN CURSO c ON pc.Cod_Curso = c.Cod_Curso
GROUP by P.DNI, P.Apellido, p.nombre

9. Listar Nombre, Descripción del curso que posea más alumnos inscriptos y del que posea
menos alumnos inscriptos durante 2016


SELECT c.nombre, c.descripcion
FROM CURSO c
INNER JOIN ALUMNO-CURSO a ON (c.Cod_Curso=a.Cod_Curso)
WHERE YEAR(c.Fecha_Creacion) = "2016"
GROUP BY c.Cod_Curso, c.nombre, c.descripcion
HAVING count(a.dni)=(
	SELECT MAX(count(a.dni))
	FROM CURSO cc
	INNER JOIN ALUMNO-CURSO aa ON (cc.Cod_Curso=aa.Cod_Curso)
	WHERE YEAR(cc.Fecha_Creacion) = "2016"
	GROUP BY cc.Cod_Curso
	)
UNION
SELECT c.nombre, c.descripcion
FROM CURSO c
INNER JOIN ALUMNO-CURSO a ON (c.Cod_Curso=a.Cod_Curso)
WHERE YEAR(c.Fecha_Creacion) = "2016"
GROUP BY c.Cod_Curso, c.nombre, c.descripcion
HAVING count(a.dni)=(
	SELECT MIN(count(a.dni))
	FROM CURSO cc
	INNER JOIN ALUMNO-CURSO aa ON (cc.Cod_Curso=aa.Cod_Curso)
	WHERE YEAR(cc.Fecha_Creacion) = "2016"
	GROUP BY cc.Cod_Curso
)

Ejercicio 5:
Zona =(IdZona, NombreZona)
Cine =(IdCine, NombreCine, IdZona, Ubicación)
Sala =(IdCine, NumeroSala, Capacidad)
Película =(IdPelícula, Título, Año, Género, Duración, Calificación, Sinopsis)
ActorDirector =(IdAD, NombreAD)
Dirección = (IdPelícula, IdAD)
INNER JOIN provincia p ON (ci.IdProvincia = p.IdProvincia)
INNER JOIN programación pr ON (ca.IdCanal = pr.IdCanal)
INNER JOIN Dibujo d ON (
Actuación =(IdPelícula, IdAD)
Función = (IdCine, NumeroSala, IdPelícula, Fecha, Horario)
(
 6. Listar los Títulos de las películas que tengan función en todas las zonas. 
SELECT p.TITULO
FROM pelicula p
WHERE not exists(
 	SELECT * 
 	FROM  Cine c INNER JOIN zona z ON (z.IdZona = c.IdZona)
 	WHERE not exists(
 		SELECT * 
 		FROM funcion f
		WHERE p.IdPelícula = f.IdPelícula AND c.IdCine = f.IdCine))


Ejercicio 7:
Provincia = (IdProvincia, Nombre)
Ciudad (IdCiudad, Nombre, IdProvincia)
Canal (IdCanal, Denominación, Dirección, Teléfono, IdCiudad)
Dibujo (IdDibujo, Nombre, Descripción, Año_Creación)
Personaje = (IdPersonaje, Nombre, Descripción, IdDibujo)
Programación = (IdCanal, IdDibujo, Fecha, Horario)


1. Listar nombre de todas las ciudades correspondientes a la provincia de nombre Bs
As que tengan algún canal.

SELECT *
FROM ciudad ci
INNER JOIN canal ca ON (ci.IdCiudad = ca.IdCiudad)
INNER JOIN provincia p ON (ci.IdProvincia = p.IdProvincia)
WHERE p.Nombre = 'Bs As'

2. Listar denominación, dirección y teléfono de todos los canales para la ciudad de
Necochea.

SELECT c.denominación,c.dirección,c.telefono
FROM canales c 
INNER JOIN ciudad ci ON (ci.IdCiudad=c.IdCiudad)
WHERE ci.nombre = "Necochea"

4. Listar la denominación, dirección y teléfono de aquellos canales que pertenezcan a
la provincia de “Santiago” y que tengan programación para el dibujo “Jorge el
curioso”. Dicho listado deberá estar ordenado por denominación.

SELECT *
FROM canal ca
INNER JOIN ciudad ci ON (ca.IdCiudad = ci.IdCiudad)
INNER JOIN provincia p ON (ci.IdProvincia = p.IdProvincia)
INNER JOIN programación pr ON (ca.IdCanal = pr.IdCanal)
INNER JOIN Dibujo d ON (pr.IdDibujo = d.IdDibujo)
WHERE p.Nombre = "Santiago" and d.Nombre = "Jorge, Ariel la concha de tu hermana"
ORDER BY c.denominación

5. Listar el nombre y año de creación de todos los dibujos que posean año de creación
entre los años 2000 y 2010 o que tengan al menos un personaje cuyo nombre
contenga la cadena “abe”. Dicho listado deberá estar ordenado por nombre.

6. Listar la descripción y el nombre de los dibujos que tengan programación para
canales de la ciudad de Lincoln y no tengan personajes con nombre que comiencen
con ‘ca’.

7. Listar la descripción y el nombre de los dibujos que tengan programación en todos
los canales.
8. Listar el Nombre y Descripción de aquellos dibujos que tengan programación en la
ciudad de “Lincoln” y no posean programación en la ciudad de “Junin”.
9. Listar denominación, dirección y teléfono de canales de Lanus o que tengan más de
20 programaciones.
10. Listar la Denominación, Dirección y Teléfono de aquellos canales que tengan
programación para más de 15 dibujos diferentes.