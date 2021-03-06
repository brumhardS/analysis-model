# Testen

Wenn möglich, nutzen wir das Prinzip des Test Driven Development. D.h. Tests werden vorab bzw. zeitgleich mit
den zu erstellenden Klassen geschrieben. Details zu diesem Ansatz finden sich in [4] in Kapitel 9. Dies hat viele
Vorteile:
- Es werden nur die Anforderungen umgesetzt, die auch wirklich nötig sind.
    - **YAGNI**: You ain‘t gonna need it!
    - **KISS**: Keep it simple, stupid!
- Das Softwaredesign orientiert sich an der Nutzung von Klassen, da die Tests die ersten "Anwender" des API sind.
- Testfälle dokumentieren eine Klasse (und ergänzen somit die Spezifikation).
- Testbarkeit eines Programms ist per Definition garantiert und zudem erhalten wir automatisch eine hohe Testabdeckung.

## Konventionen beim Schreiben von Modultests

Wir nutzen für Modultests (d.h. Unittests) die [JUnit](http://junit.org/) Bibliothek. Alle Modultests einer Klasse `Foo` 
legen wir in der zugehörigen Klasse `FooTest` ab. Die Tests werden im Verzeichnis *src/test/java* abgelegt, damit
sie separat von den eigentlichen Klassen liegen (diese liegen unter  *src/main/java*). 
 
Ein Modultest besteht immer aus drei Schritten, die ggf. zusammenfallen können:

1. **Given**: Das zu testende Objekt wird erzeugt (Subject Under Test: SUT). Sind dazu weitere Objekte nötig, 
so werden diese in diesem Schritt ebenso erzeugt. 
2. **When**: Die zu überprüfende Funktionalität wird aufgerufen. Sind dazu weitere Objekte nötig (z.B. als Methodenparameter),
sollten diese bereits in Schritt 1.) erzeugt werden.
3. **Then**: Es wird überprüft, ob die im letzten Schritt aufgerufene Funktionalität korrekt ist. Dazu kann z.B. der
Rückgabewert einer Methode oder der innere Zustand einer Klasse herangezogen werden. Zum Prüfen verwenden wir Assertions
des JUnit Frameworks [AssertJ](http://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html)

Die Benennung der drei Schritte in **Given-When-Then** stammt aus dem 
[Behavior-Driven-Development](http://dannorth.net/introducing-bdd/) und ist in einem 
[Artikel von Martin Fowler](http://martinfowler.com/bliki/GivenWhenThen.html) gut beschrieben. 

Damit im Fehlerfall schnell die Ursache gefunden wird, benennen wir eine Testmethode mit einem sinnvollen (und langen) Namen
und ergänzen im JavaDoc in einem knappen Satz das Ziel des Tests. Eine sinnvolle Namenskonvention für Tests ist der 
Präfix *should* mit einer angehängten Beschreibung, die die Eigenschaften des SUT beschreiben, die im Test überprüft werden 
(bzw. das Ziel des Tests). 

An einem Beispiel lassen sich diese Konventionen am besten erkennen:
```java
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
 
/**
 * Tests the class {@link MathUtils}.
 *
 * @author Ullrich Hafner
 */
public class MathUtilsTest {
    /** Verifies that {@link MathUtils#max} works with positive and negative values. */
    @Test
    public void shouldFindMaximumForPositiveAndNegativeValues() {
        // Given
        MathUtils utils = new MathUtils();
        int[] inputValues = {1, -2, 0};
        
        // When
        int actual = utils.max(inputValues);
 
        // Then
        assertThat(actual).as("Wrong maximum for %s", Arrays.toString(inputValues)).isEqualTo(1);
    }
}
```

## Testen von Exceptions

Kann ein Konstruktor oder eine Methode eine Exception werfen, so muss dies auch getestet werden. Dazu wird das gleiche
Vorgehen verwendet, d.h. der Test wird in **Given-When-Then** aufgeteilt. Am elegantesten wird ein Exception
Test mit [Lambda-Ausdrücken](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/Lambda-QuickStart/index.html) 
und der Assertion `assertThatThrownBy` aus AssertJ:
 
```java
/** Verifies that at least one number is provided. */
@Test
public void shouldThrowExceptionIfArrayIsEmpty() {
    // Given
    MathUtils utils = new MathUtils();
    assertThatThrownBy(() -> utils.max(new int[0])) // When 
            .isInstanceOf(IllegalArgumentException.class) // Then
            .hasMessageContaining("empty");
 }
``` 
Wichtig ist, dass der Lambda Block nur genau die Anweisung enthält, die die Exception wirft. Dies hat den Vorteil -
auch gegenüber dem JUnit Pedant `@Test(expected = Exception.class)` - dass die Exception nur an der genau bestimmten
 Stelle überprüft wird. Wird eine Exception zufällig an einer anderen Stelle geworfen, so wird das als Testfehler
  markiert.

## Allgemeine Testszenarien

In JUnit gibt es die Möglichkeit, sich ein oder mehrere Testszenarien über speziell dafür markierte Methoden aufzubauen.
Dazu müssen diese Methoden mit `@Before`, `@BeforeClass`, `@After, etc. annotiert werden, und die erzeugten SUT (und 
abhängigen Objekte) in Objektvariablen (Fields) gespeichert werden. Dieses Vorgehen ist bequem, macht Testfalle jedoch
unübersichtlich und schwer verständlich, da die im Test verwendenten Objekte nicht direkt sichtbar sind. 
Daher verwenden wir diese Annotationen nicht. Generell gilt: Test Klassen sollen keine Objektvariablen besitzen. Statt 
dessen sollten benötigte Objekte immer neu mit passenden **create** Methoden erzeugt werden: so können die erzeugten 
Objekte in den einzelnen Tests unabhängig voneinander geändert werden können.

## Testen von Basisklassen

Abstrakte Klassen und Schnittstellen lassen sich ebenso testen: dazu wird das 
Abstract Test Pattern benutzt, das in einem [eigenen Abschnitt](Abstract-Test-Pattern.md) beschrieben ist.

## Aussagekräftige Fehlermeldungen

Ein wichtiger Schritt im TDD ist die Validierung, ob ein Test überhaupt korrekt ist. D.h. es wird sichergestellt, 
dass ein Fehler entsteht, wenn die zu testende Methode noch unvollständig ist. An dieser Stelle lohnt es sich,
die Fehlermeldung zu analysieren: ist diese nicht aussagekräftig, sollte diese mit der Methode `as` entsprechend 
verbessert werden: 

```java
assertThat(list.size()).as("Wrong number of list elements").isEqualTo(5);
```   

## Eigenschaften von Modultests

Gut geschriebene Modultests lassen sich nach dem [FIRST](https://pragprog.com/magazines/2012-01/unit-tests-are-first)
Prinzip charakterisieren, das sich folgendermaßen zusammenfassen lässt:
- Sie sind unabhängig und können in beliebiger Reihenfolgen laufen.
- Sie sind konsistent und liefern immer das gleiche Resultat.
- Sie können schnell und automatisiert ausgeführt werden.
- Sie sind verständlich und wartungsfreundlich.

## Tipps und Tricks

Hier noch einige Anregungen bei der Gestaltung von Modultests:
- Eine Testmethode sollte nur einen Aspekt testen: d.h. wir testen ein bestimmtes Verhalten und nur indirekt eine Methode.
- Testmethoden sollten ca. 5-15 Zeilen umfassen.
- Modultests greifen i.A. nie auf Datenbank, Dateisystem oder Web Services zu.
- Häufig verwendete Eingangsparameter sollten als Konstanten definiert werden. 
- Tests nutzen die selben Kodierungsrichtlinien wie normale Klassen.

## Testfall Anzahl

Die Anzahl der erforderlichen Tests für eine Klasse lässt sich schwer herleiten. Daher sollten folgende Kriterien
herangezogen werden:
- Jede nicht-triviale public Methode einer Klasse mit mindestens einem Testfall überprüfen
  - Randwerte (0, -1, 1, etc.) verwenden
  - Eingabeparameter mit unerwarteten Werten (null, {}, etc.) belegen
- Äquivalenzklassen bilden: minimale Anzahl Tests für maximale Variation der Testdaten
- Zu jedem entdeckten Fehler (z.B. durch einen Bug Report) einen passenden Testfall erstellen

Sinnvollerweise nutzt man die [Coverage Übersicht](https://www.jetbrains.com/idea/help/code-coverage.html) 
der Entwicklungsumgebung, um zu überprüfen, welcher Teil des Quelltextes bereits getestet wurde.

## State Based vs. Interaction Based Testing

Prinzipiell gibt es zwei Varianten des Testings. Beim **State Based Testing** wird das Testobjekt nach Aufruf der zu 
testenden Methoden durch Abfrage seines internen Zustands verifiziert. Analog dazu kann natürlich auch der Zustand 
der im Test verwendeten Parameter bzw. Rückgabewerte analysiert werden. Alle bisher beschriebenen Tests laufen nach diesem
Muster ab und können folgendermaßen formuliert werden:

```java
/** [Kurze Beschreibung: was genau macht der Test] */
@Test
public void should[restlicher Methodenname der den Test fachlich beschreibt]() {
    // Given
    [Test Setup: Erzeugung der Parameter, die das SUT zum Erzeugen bzw. beim Aufruf benötigt]
    [Erzeugung des SUT]
    // When
    [Aufruf der zu testenden Methoden]
    // Then
    [Verifikation des Zustands des SUT bzw. von Parametern oder Rückgabewerten mittels AssertJ]
}
```

Im Gegensatz dazu wird beim **Interaction Based Testing** nicht der Zustand des SUT analysiert. Statt dessen werden die 
Aufrufe aller am Test beteiligten Objekte mit einem Mocking Framework wie [Mockito](http://site.mockito.org/) überprüft.
D.h. hier steht nicht der Zustand des Testobjekts im Vordergrund, sondern die Interaktion mit beteiligten Objekten. Ein
typischer Testfall nach dem Interaction Based Testing ist folgendermaßen aufgebaut:

```java
/** [Kurze Beschreibung: was genau macht der Test] */
@Test
public void should[restlicher Methodenname der den Test fachliche beschreibt]() {
    // Given
    [Test Setup 1: Erzeugung der Mocks, die zur Verifikation benötigt werden]
    [Test Setup 2: Erzeugung der Stubs, die das SUT zum Erzeugen bzw. beim Aufruf benötigt]
    [Erzeugung des SUT]
    // When
    [Aufruf der zu testenden Methoden]
    // Then
    [Verifikation des Zustands der Mocks]
}
```

Details zu diesem Vorgehen werden später in einem separaten Abschnitt beschrieben.
