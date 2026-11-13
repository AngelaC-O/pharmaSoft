whenever sqlerror exit sql.sqlcode rollback
begin
    if sys_context('USERENV', 'SESSION_USER') <> 'PHARMA_S14' then
        raise_application_error(-20001, 'Ejecutar solamente con el usuario de pruebas PHARMA_S14');
    end if;
end;
/
merge into categorias c
using (select 'Medicamentos S14' nombre from dual) s
on (c.nombre=s.nombre)
when not matched then insert (nombre, estado, fecha_creacion, creado_por)
values (s.nombre, true, systimestamp, 'carga');

merge into clientes c
using (select '70140140' dni from dual) s
on (c.dni=s.dni)
when not matched then insert (dni, nombres, apellidos, email, estado, fecha_creacion, creado_por)
values (s.dni, 'Ana', 'Mamani S14', 'mamani.s14@example.test', true, systimestamp, 'carga');

merge into productos p
using (select 'Paracetamol S14' nombre from dual) s
on (p.nombre=s.nombre)
when not matched then insert (nombre, precio, stock, estado, categoria_id, fecha_creacion, creado_por)
values (s.nombre, 6.25, 2000, true, (select min(id) from categorias), systimestamp, 'carga');

insert into productos (nombre, precio, stock, estado, categoria_id, fecha_creacion, creado_por)
select 'Producto de prueba ' || lpad(level, 5, '0'), round(dbms_random.value(1, 200), 2),
       trunc(dbms_random.value(0, 500)), true, (select min(id) from categorias), systimestamp, 'carga'
from dual
where not exists (select 1 from productos where nombre = 'Producto de prueba 00001')
connect by level <= 5000;

insert into ventas (fecha, total, estado, cliente_id, creado_por)
select timestamp '2026-11-12 12:00:00' - numtodsinterval(dbms_random.value(0, 365), 'DAY'),
       round(dbms_random.value(5, 500), 2), 'REGISTRADA', (select min(id) from clientes), 'carga'
from dual
where not exists (select 1 from ventas where creado_por = 'carga')
connect by level <= 20000;
commit;
select count(*) productos from productos;
select count(*) ventas from ventas;
