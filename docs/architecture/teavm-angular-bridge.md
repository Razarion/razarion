# TeaVM WASM ↔ Angular TypeScript Bridge

This document explains how method calls work bidirectionally between Angular TypeScript and Java WebAssembly (compiled via TeaVM).

## 🌉 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Angular TypeScript                        │
│  ┌────────────────────────────────────────────────────┐     │
│  │  window.gwtAngularFacade (Bridge Object)           │     │
│  │  ┌──────────────────┐  ┌──────────────────┐       │     │
│  │  │ TS → Java        │  │ Java → TS        │       │     │
│  │  │ (Interfaces)     │  │ (Proxy Objects)  │       │     │
│  │  └──────────────────┘  └──────────────────┘       │     │
│  └────────────────────────────────────────────────────┘     │
└────────────────────┬──────────────────┬─────────────────────┘
                     │                  │
              @JSBody calls      @JSFunctor callbacks
                     │                  │
┌────────────────────▼──────────────────▼─────────────────────┐
│              Java WASM (TeaVM compiled)                      │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  JsGwtAngularFacade (Java Side)                     │    │
│  │  - Reads TS objects via @JSBody                     │    │
│  │  - Creates JS proxies via @JSFunctor                │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

## 1️⃣ TypeScript → Java (Angular calls Java)

### Step 1: Angular sets interfaces on `window.gwtAngularFacade`

```typescript
// game.component.ts
window.gwtAngularFacade = new class extends GwtAngularFacade {
  screenCover = {
    setLoadingScreen: (message: string) => {
      // Angular implementation
      this.loadingMessage = message;
    }
  };

  babylonRenderServiceAccess = {
    createTerrainMesh: (groundHeightMap: Uint16Array, ...) => {
      // Babylon.js rendering
      return this.createMesh(...);
    }
  };
};
```

### Step 2: Java reads these objects via `@JSBody`

```java
// JsGwtAngularFacade.java
public class JsGwtAngularFacade {
    @JSBody(script = "return window.gwtAngularFacade;")
    private static native JSObject getWindowFacade();

    @JSBody(params = {"facade"}, script = "return facade.screenCover;")
    private static native JSObject getScreenCoverJs(JSObject facade);

    public ScreenCover getScreenCoverAdapter() {
        JSObject js = getScreenCoverJs(facade);
        return new JsScreenCover(js);  // Wrapper
    }
}
```

### Step 3: Java calls TypeScript methods

```java
// JsScreenCover.java
public class JsScreenCover implements ScreenCover {
    private final JSObject js;

    @Override
    public void setLoadingScreen(String message) {
        callSetLoadingScreen(js, message);  // Calls TS!
    }

    @JSBody(params = {"obj", "msg"},
            script = "obj.setLoadingScreen(msg);")
    private static native void callSetLoadingScreen(JSObject obj, String msg);
}
```

**Data Flow:**
```
Java Code
  → callSetLoadingScreen(@JSBody)
    → JavaScript Bridge
      → TypeScript window.gwtAngularFacade.screenCover.setLoadingScreen()
        → Angular Component Update
```

## 2️⃣ Java → TypeScript (Java calls Angular)

### Step 1: Java creates JS proxy objects via `@JSFunctor`

```java
// AngularProxyFactory.java
@JSFunctor
public interface VoidWithIntCallback extends JSObject {
    void call(int arg);
}

@JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
private static native void setMethodInt(JSObject obj, String name,
                                       VoidWithIntCallback fn);

public static JSObject createInputServiceProxy(InputService service) {
    JsObject proxy = JsObject.create();

    // Lambda is converted to callable JS function!
    setMethodInt(proxy, "onMouseDown", (button) -> {
        service.onMouseDown(button);  // Java code
    });

    return proxy;
}
```

**How does `@JSFunctor` work?**
- TeaVM generates a **WASM-to-JS wrapper function**
- The lambda `(button) -> service.onMouseDown(button)` is exported as a **callable JavaScript function**
- TypeScript can call this function directly: `inputService.onMouseDown(0)`

### Step 2: Java registers proxy on `window.gwtAngularFacade`

```java
// TeaVMGwtAngularService.java
public void init() {
    JsGwtAngularFacade facade = JsGwtAngularFacade.get();

    // Create proxies for all Java services
    JSObject inputProxy = AngularProxyFactory.createInputServiceProxy(inputService);
    facade.setJavaService("inputService", inputProxy);

    JSObject gameUiProxy = AngularProxyFactory.createGameUiControlProxy(gameUiControl);
    facade.setJavaService("gameUiControl", gameUiProxy);
}
```

**Result:**
```javascript
window.gwtAngularFacade = {
  // Angular → Java (set by TS)
  screenCover: { setLoadingScreen: (msg) => {...} },
  babylonRenderServiceAccess: { createTerrainMesh: (...) => {...} },

  // Java → Angular (set by Java)
  inputService: { onMouseDown: (button) => {/* calls WASM */} },
  gameUiControl: { getPlanetConfig: () => {/* calls WASM */} }
}
```

### Step 3: TypeScript calls Java methods

```typescript
// input-handler.service.ts
export class InputHandlerService {
  onMouseDown(event: MouseEvent) {
    const button = event.button;

    // Calls Java WASM!
    window.gwtAngularFacade.inputService.onMouseDown(button);
  }
}
```

**Data Flow:**
```
Angular Event Handler
  → window.gwtAngularFacade.inputService.onMouseDown(0)
    → @JSFunctor Wrapper (JS → WASM Bridge)
      → Java Lambda: (button) -> service.onMouseDown(button)
        → InputService.onMouseDown(0) // Java WASM Code
```

## 🔄 Complete Example: Mouse Click

```typescript
// 1. User clicks in Angular
@HostListener('mousedown', ['$event'])
onMouseDown(event: MouseEvent) {
  // 2. TypeScript calls Java proxy
  window.gwtAngularFacade.inputService.onMouseDown(event.button);
}
```

```java
// 3. @JSFunctor lambda is executed
setMethodInt(proxy, "onMouseDown", (button) -> {
    service.onMouseDown(button);  // 4. Calls Java service
});

// 5. InputService processes event
public void onMouseDown(int button) {
    // Java business logic...
    DecimalPosition terrainPos = calculateTerrainPosition();

    // 6. Java calls Angular rendering
    screenCover.showSelectionMarker(terrainPos.getX(), terrainPos.getY());
}
```

```typescript
// 7. @JSBody calls TypeScript
screenCover = {
  showSelectionMarker: (x: number, y: number) => {
    // 8. Angular updates UI
    this.selectionMesh.position.set(x, 0, y);
  }
};
```

## ⚡ WASM-GC Specifics

### Problem: JSObject Type Checking
```java
// ❌ DOES NOT WORK in WASM-GC:
Uint16ArrayEmu heightMap = ...; // is JSObject
int value = heightMap.getAt(0); // IllegalCast!
```

**Reason:** In WASM-GC, when Java code calls methods on an interface (like `Uint16ArrayEmu`), TeaVM performs strict runtime type checking. When the implementation is actually a `JSObject` (JavaScript object), and that call happens from pure Java code context (like `TerrainAnalyzer`), the type check fails with "illegal cast".

**Solution:** Dependency Injection with platform-specific converters
```java
// ✅ WORKS:
@Inject HeightMapConverter converter;  // Platform-specific!

int[] javaArray = converter.convert(heightMap);  // Safe conversion in JS context
```

**Pattern:**
1. **Interface in shared code:** `HeightMapConverter` (in `razarion-ui-service`)
2. **Client implementation:** `TeaVMHeightMapConverter` casts to `JSObject` and uses static `@JSBody` method
3. **Test implementation:** `TestHeightMapConverter` just calls `toJavaArray()`
4. **Dagger binds** the correct implementation per platform

```java
// TeaVMHeightMapConverter.java (client-specific)
@Singleton
public class TeaVMHeightMapConverter implements HeightMapConverter {
    @Override
    public int[] convert(Uint16ArrayEmu heightMap) {
        // Cast to JSObject (type check only, no method call)
        if (heightMap instanceof JSObject) {
            JSObject jsObj = (JSObject) heightMap;
            // Static @JSBody method - executes in JS context
            return JsUint16ArrayWrapper.convertToJavaArray(jsObj);
        }
        return heightMap.toJavaArray();
    }
}

// JsUint16ArrayWrapper.java
@JSBody(params = {"jsArray"}, script =
    "var len = jsArray.length; " +
    "var result = []; " +
    "for (var i = 0; i < len; i++) { result[i] = jsArray[i]; } " +
    "return result;")
public static native int[] convertToJavaArray(JSObject jsArray);
```

**Why this works:**
- Conversion happens in **JavaScript context** via `@JSBody`
- Returns plain Java `int[]` that can be accessed anywhere
- No JSObject methods called from Java execution context

## 📋 @JSBody vs @JSFunctor

| Feature | `@JSBody` | `@JSFunctor` |
|---------|-----------|--------------|
| **Purpose** | Java → JS calls | JS → Java callbacks |
| **Direction** | Java calls JS | JS calls Java |
| **Return** | Can return values | Only lambda/callback |
| **Example** | `callMethod(obj, "name")` | `setMethod(obj, "name", () -> {...})` |
| **Type Safety** | Minimal (script as string) | Strong (typed parameters) |
| **WASM-GC** | Works for static calls | Requires typed interface! |

### Critical WASM-GC Rule for @JSFunctor:

**❌ WRONG - Generic JSObject parameter:**
```java
@JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
private static native void setMethod(JSObject obj, String name, JSObject fn);

// TeaVM can't determine how to wrap the lambda!
setMethod(proxy, "onClick", () -> handleClick());  // Type error
```

**✅ CORRECT - Typed @JSFunctor interface:**
```java
@JSFunctor
public interface VoidCallback extends JSObject {
    void call();
}

@JSBody(params = {"obj", "name", "fn"}, script = "obj[name] = fn;")
private static native void setMethod(JSObject obj, String name, VoidCallback fn);

// TeaVM knows this is a callable function!
setMethod(proxy, "onClick", () -> handleClick());  // Works!
```

### Why Method References Don't Work:

**❌ WRONG:**
```java
setGetterDouble(obj, "getRadius", placer::getRadius);  // Illegal cast in WASM-GC
```

**✅ CORRECT:**
```java
setGetterDouble(obj, "getRadius", () -> placer.getRadius());  // Works!
```

**Reason:** Method references like `placer::getRadius` don't properly cross the Java-JS boundary in WASM-GC. Always use **explicit lambdas**.

## 🔧 Key Files

| File | Purpose |
|------|---------|
| `JsGwtAngularFacade.java` | Central bridge, reads `window.gwtAngularFacade` |
| `AngularProxyFactory.java` | Creates JS proxies from Java services using `@JSFunctor` |
| `DtoConverter.java` | Converts Java DTOs to JS objects (for passing complex data) |
| `TeaVMGwtAngularService.java` | Initializes bridge, registers all Java services |
| `GwtAngularFacade.ts` | TypeScript interfaces for the bridge |
| `game.component.ts` | Angular side, provides implementations to Java |

## 📦 Summary

1. **`window.gwtAngularFacade`** = Central bidirectional bridge
2. **`@JSBody`** = Java directly calls TypeScript methods
3. **`@JSFunctor`** = TypeScript can call Java lambdas as functions
4. **Proxy Pattern** = Java objects converted to callable JS objects
5. **Type Safety** = TeaVM WASM-GC enforces strict type checks → Converter pattern needed
6. **Explicit Lambdas** = Always use `() -> method()`, never `this::method`
7. **Typed Functors** = Each parameter type needs its own `@JSFunctor` interface

## 🚀 Performance

- `@JSBody` calls: ~5-10µs (direct)
- `@JSFunctor` callbacks: ~10-20µs (WASM↔JS boundary)
- Significantly faster than old GWT2 JSNI calls!
- **Critical:** Avoid frequent small calls across boundary - batch operations when possible

## 🐛 Common Issues

### 1. "illegal cast" errors
**Cause:** Calling methods on JSObject-backed interfaces from pure Java code
**Fix:** Use platform-specific converters injected via Dagger

### 2. "undefined is not a function"
**Cause:** Using method references instead of lambdas
**Fix:** Change `obj::method` to `() -> obj.method()`

### 3. Null/undefined errors in marshalling
**Cause:** JavaScript `undefined` cannot be converted to Java types
**Fix:** Check with `JsUtils.isNullOrUndefined()` before conversion

### 4. Type mismatch in @JSFunctor
**Cause:** Using generic `JSObject` parameter instead of typed `@JSFunctor` interface
**Fix:** Create specific `@JSFunctor` interface for each callback signature
