# Tarea SMA JADE

## Integrantes

- 
- 
- 

## Descripción

Proyecto de **sistema multiagente (SMA)** con el framework **JADE** orientado a un escenario de **monitoreo de cultivo**. Incluye agentes que simulan sensores de temperatura y humedad (registrados en el Directorio de Facilitadores, páginas amarillas), un agente **analizador** que consulta periódicamente a los sensores y un agente de **control de riego** que recibe órdenes según el análisis. La aplicación se empaqueta como un JAR con dependencias y puede ejecutarse en **Docker Compose** con dos contenedores: uno aloja la plataforma principal con los sensores y otro un contenedor secundario conectado al primero, donde corren el analizador y el control de riego.

## Requisitos previos

- **Java 17** (o compatible con la configuración del `pom.xml`)
- **Maven**
- **Docker** y el plugin **Compose** (`docker compose`)
- El archivo **`jade-4.5.0.jar`** en la raíz del proyecto (o la ruta que indiques en `-Dfile=`) para instalarlo en el repositorio local de Maven

## Cómo correrlo

Ejecuta los pasos en orden desde la raíz del repositorio.

### 1. Instalar JADE en el repositorio local de Maven

```bash
mvn install:install-file "-Dfile=jade-4.5.0.jar" "-DgroupId=com.tilab.jade" "-DartifactId=jade" "-Dversion=4.5.0" "-Dpackaging=jar"
```

**Qué hace:** el goal `install:install-file` de Maven registra un JAR que no está en Maven Central en tu **repositorio local** (`~/.m2/repository`), para que el `pom.xml` pueda resolver la dependencia.

- **`-Dfile=...`**: ruta al archivo JAR de JADE que vas a publicar localmente.
- **`-DgroupId=...`**: identificador de grupo (debe coincidir con el `<groupId>` de la dependencia en el `pom.xml`).
- **`-DartifactId=...`**: nombre del artefacto (`jade`).
- **`-Dversion=...`**: versión (`4.5.0`).
- **`-Dpackaging=jar`**: tipo de empaquetado.

### 2. Compilar y generar el JAR ejecutable

```bash
mvn clean package -DskipTests
```

**Qué hace:**

- **`clean`**: borra la carpeta `target` para evitar restos de compilaciones anteriores.
- **`package`**: compila el código y ejecuta el empaquetado (incluye el assembly *jar-with-dependencies* con `jade.Boot` como clase principal).
- **`-DskipTests`**: omite la fase de tests (útil si no hay tests o quieres acelerar el build).

El JAR resultante (con sufijo `-jar-with-dependencies`) es el que copian los Dockerfiles como `app.jar`.

### 3. Construir las imágenes de Docker

```bash
docker compose build
```

**Qué hace:** lee `docker-compose.yml` y construye las imágenes definidas en los servicios (`jade-p1`, `jade-p2`) usando los `Dockerfile` indicados, incorporando el JAR ya construido.

### 4. Levantar los contenedores

```bash
docker compose up
```

**Qué hace:** crea la red, inicia los servicios en el orden definido (`jade-p2` depende de `jade-p1`), expone los puertos configurados (por ejemplo **1099** para el contenedor principal de JADE) y muestra los logs en la consola. Para ejecutarlo en segundo plano puedes usar `docker compose up -d`.

Para detener: `Ctrl+C` (si está en primer plano) o `docker compose down`.

---

*Curso: Software Inteligente — Tarea SMA JADE.*
