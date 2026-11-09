# Taller #5 — Programación 1

**Tema:** 2.9 Entrada y salida en Java
- 2.9.1 Entrada con `JOptionPane`
- 2.9.2 Salida con `JOptionPane`
- 2.9.3 Entrada con `BufferedReader`
- 2.9.4 Salida en consola con `System.out` (complemento de `BufferedReader`)

**Curso:** Programación 1 — Universidad
**Docente:** SarmientoEdu
**Modalidad:** Individual
**Duración estimada:** 60–90 minutos

---

## 📋 Contenido

| Ejercicio | Archivo | Técnica de E/S |
|-----------|---------|----------------|
| 1. Calculadora de propina | `src/taller5/Ejercicio1.java` | `JOptionPane` |
| 2. Promedio de 3 notas | `src/taller5/Ejercicio2.java` | `BufferedReader` |
| 3. Área/perímetro rectángulo | `src/taller5/Ejercicio3.java` | `JOptionPane` |
| 4. Conversión de temperatura | `src/taller5/Ejercicio4.java` | `BufferedReader` |
| 5. Clase `Estudiante` | `src/taller5/Ejercicio5.java` + `Estudiante.java` | Mixto |

---

## ✅ Lo que SÍ puedes usar

- `JOptionPane.showInputDialog` / `showMessageDialog`
- `BufferedReader` + `InputStreamReader` + `System.in`
- `System.out.println` / `System.out.printf`
- Clases, objetos, métodos, constructores, getters/setters
- `private` / `public`
- `String.format`, `Double.parseDouble`, `Integer.parseInt`
- Condicionales `if/else`, operadores aritméticos, comparación

## ❌ Lo que NO debes usar (el autograde lo detecta)

- `Scanner` (usa `BufferedReader` o `JOptionPane`)
- `System.console()`
- `FileReader`, `FileWriter`, `BufferedWriter`
- `ArrayList`, `HashMap`, `HashSet`, `LinkedList`
- Herencia, `extends`, `implements`, `interface`, `abstract`
- Librerías externas
- Hardcodear resultados

---

## 🧪 Cómo se califica

El autograde corre 15 semillas derivadas de los últimos 3 dígitos de tu cédula
(que escribes en `informe.md`), y para cada una invoca por reflexión los métodos
públicos de los ejercicios con las firmas exactas descritas en el enunciado.
Compara tu salida con la esperada con tolerancia de ±0.01.

**Nota = Ej1(15) + Ej2(15) + Ej3(15) + Ej4(15) + Ej5(20) + Git(10) + Evidencias(10)**

Si la nota es menor a 60, el workflow falla (aparece en rojo). Es normal; corrige y vuelve a hacer push.

---

## 🚀 Flujo de entrega

1. **Fork** de este repo a tu cuenta.
2. **Habilitar Actions** en tu fork: Settings → Actions → General → "Read repository contents".
3. **Clonar** tu fork:
   ```bash
   git clone https://github.com/TU-USUARIO/Taller5-java.git
   cd Taller5-java

4. **Crear rama**: git checkout -b lab10-apellido-nombre
5. **Configurar identidad**:
   ```bash
   git config user.name "Tu Nombre"
   git config user.email "tu-correo@ejemplo.com"

6. Trabajar los 5 ejercicios en src/taller5/.
7. Commits por ejercicio (mínimo uno por ejercicio, con prefijo):
   ```bash
   git commit -m "ej1: calculadora de propina con JOptionPane"
   git commit -m "ej2: promedio con BufferedReader"
   git commit -m "ej3: área y perímetro con JOptionPane"
   git commit -m "ej4: conversión Celsius-Fahrenheit"
   git commit -m "ej5: clase Estudiante + E/S mixta"
   git commit -m "evidencias: capturas de los 5 ejercicios y del job de Actions"
   git commit -m "informe: datos del estudiante"

8. **Push**: git push origin lab10-apellido-nombre
9. Abrir Pull Request desde tu rama hacia SarmientoEdu:main. No lo fusiones.
10. Revisar el resultado en la pestaña Actions de tu fork (Job Summary y artifact resultado-autograde).

