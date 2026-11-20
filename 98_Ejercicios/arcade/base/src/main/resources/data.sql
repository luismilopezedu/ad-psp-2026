INSERT INTO plataforma VALUES ('PC','Ordenador'),('SW','Portátil'),('VR','Visor');
INSERT INTO juego(id,titulo,genero,precio,plataforma_id) VALUES
 ('J1','Luna roja','PUZLE',8,'PC'),('J2','Ruta lunar','CARRERAS',20,'PC'),
 ('J3','Mareas','AVENTURA',30,'PC'),('J4','Luna azul','PUZLE',25,'SW'),
 ('J5','Rally','CARRERAS',12,'SW'),('J6','Eco','AVENTURA',5,'VR');
INSERT INTO logro VALUES ('L1','Inicio','J1'),('L2','Meta','J1'),('L3','Meta','J3'),('L4','Inicio','J4');
