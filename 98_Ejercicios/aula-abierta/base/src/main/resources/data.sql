INSERT INTO area(id,nombre) VALUES ('A','Tecnología'),('B','Diseño'),('C','Idiomas');
INSERT INTO curso(id,titulo,nivel,horas,area_id,retirado) VALUES
 ('C1','Java inicial','BASICO',10,'A',false),
 ('C2','Java web','MEDIO',20,'A',false),
 ('C3','Datos con Java','AVANZADO',30,'A',false),
 ('C4','Color inicial','BASICO',25,'B',false),
 ('C5','Diseño web','MEDIO',12,'B',false),
 ('C6','Inglés oral','AVANZADO',8,'C',false);
INSERT INTO leccion(id,titulo,curso_id) VALUES
 ('L1','Variables','C1'),('L2','Práctica','C1'),('L3','HTTP','C2'),
 ('L4','Práctica','C3'),('L5','Paletas','C4');
