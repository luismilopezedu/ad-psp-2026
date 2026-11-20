"""Comprueba una sesión sobre un arranque limpio en localhost:8080."""
import argparse
import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen

parser = argparse.ArgumentParser()
parser.add_argument("ejercicio", choices=list("1234567"))
parser.add_argument("perfil", nargs="?", choices=["local", "demo"], default="local")
args = parser.parse_args()
casos = json.loads(Path(__file__).with_name("casos.json").read_text())[args.ejercicio]
fallos = 0
for metodo, ruta, cuerpo, estado, esperado in casos:
    if args.ejercicio == "5" and args.perfil == "demo":
        esperado = {"/config/modo":"DEMO", "/config/moneda":"USD", "/config/limite":2,
                    "/config/resumen":{"modo":"DEMO","moneda":"USD","limite":2},
                    "/config/importe/J1":{"precio":8,"moneda":"USD"}}[ruta]
    peticion = Request("http://localhost:8080" + ruta, method=metodo,
                       data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                       headers={"Content-Type": "application/json"})
    try:
        respuesta = urlopen(peticion, timeout=6)
    except HTTPError as error:
        respuesta = error
    with respuesta:
        texto = respuesta.read().decode()
        try:
            valor = json.loads(texto)
        except ValueError:
            valor = texto or None
        correcto = respuesta.status == estado and (estado >= 400 or valor == esperado)
        print("OK" if correcto else "FALLO", metodo, ruta, respuesta.status)
        if not correcto:
            print("  Esperado:", estado, esperado, "\n  Recibido:", valor)
            fallos += 1
raise SystemExit(bool(fallos))
