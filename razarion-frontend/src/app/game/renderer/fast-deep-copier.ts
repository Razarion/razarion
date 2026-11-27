import {DeepCopier} from "@babylonjs/core/Misc/deepCopier";
import {Logger} from "@babylonjs/core/Misc/logger";

/**
 * Babylon's DeepCopier.DeepCopy, with the property names collected in linear instead of quadratic
 * time. Everything else is a copy of @babylonjs/core 9.2.1 Misc/deepCopier.js, line for line.
 * <p>
 * Every Mesh.clone runs DeepCopy over the source mesh, and DeepCopy starts by collecting the
 * names of all properties along the prototype chain - several hundred for a Mesh - deduplicating
 * them with indexOf on the growing list. A CPU profile of a Pixel 7 in a fight (2026-11-27) put 48 %
 * of the time spent building a unit's Babylon item into that one helper, GetAllPropertyNames: a
 * unit coming into view cost 13-37 ms, and the follow camera bringing six into view in one tick
 * made that tick 111 ms - a visible jerk. A unit is built from a clone of every mesh of its model.
 * <p>
 * The names come out in exactly the order of the original - the object's own first, then each
 * prototype's new ones, nearest first - because the copy assigns them in that order and a setter
 * may depend on what was set before it.
 */
const CloneValue = (source: any, destinationObject: any, shallowCopyValues: boolean): any => {
  if (!source) {
    return null;
  }
  if (source.getClassName && source.getClassName() === "Mesh") {
    return null;
  }
  if (source.getClassName && (source.getClassName() === "SubMesh" || source.getClassName() === "PhysicsBody")) {
    return source.clone(destinationObject);
  } else if (source.clone) {
    return source.clone();
  } else if (Array.isArray(source)) {
    return source.slice();
  } else if (shallowCopyValues && typeof source === "object") {
    return {...source};
  }
  return null;
};

export function getAllPropertyNames(obj: any): string[] {
  const seen = new Set<string>();
  const props: string[] = [];
  do {
    for (const prop of Object.getOwnPropertyNames(obj)) {
      if (!seen.has(prop)) {
        seen.add(prop);
        props.push(prop);
      }
    }
  } while ((obj = Object.getPrototypeOf(obj)));
  return props;
}

export function fastDeepCopy(source: any, destination: any, doNotCopyList?: string[], mustCopyList?: string[], shallowCopyValues = false): void {
  const properties = getAllPropertyNames(source);
  for (const prop of properties) {
    if (prop[0] === "_" && (!mustCopyList || mustCopyList.indexOf(prop) === -1)) {
      continue;
    }
    if (prop.endsWith("Observable")) {
      continue;
    }
    if (doNotCopyList && doNotCopyList.indexOf(prop) !== -1) {
      continue;
    }
    const sourceValue = source[prop];
    const typeOfSourceValue = typeof sourceValue;
    if (typeOfSourceValue === "function") {
      continue;
    }
    try {
      if (typeOfSourceValue === "object") {
        if (sourceValue instanceof Uint8Array) {
          destination[prop] = Uint8Array.from(sourceValue);
        } else if (sourceValue instanceof Array) {
          destination[prop] = [];
          if (sourceValue.length > 0) {
            if (typeof sourceValue[0] == "object") {
              for (let index = 0; index < sourceValue.length; index++) {
                const clonedValue = CloneValue(sourceValue[index], destination, shallowCopyValues);
                if (destination[prop].indexOf(clonedValue) === -1) {
                  // Test if auto inject was not done
                  destination[prop].push(clonedValue);
                }
              }
            } else {
              destination[prop] = sourceValue.slice(0);
            }
          }
        } else {
          destination[prop] = CloneValue(sourceValue, destination, shallowCopyValues);
        }
      } else {
        destination[prop] = sourceValue;
      }
    } catch (e: any) {
      // Log a warning (it could be because of a read-only property)
      Logger.Warn(e.message);
    }
  }
}

/**
 * Puts {@link fastDeepCopy} in place of Babylon's. Idempotent. ?babyloncopy in the URL keeps
 * Babylon's own, to compare the two on one device (RenderTelemetry creates / createMs).
 */
export function installFastDeepCopier(): boolean {
  try {
    if (typeof location !== 'undefined' && new URLSearchParams(location.search).has('babyloncopy')) {
      return false;
    }
  } catch {
    // no URL, no switch
  }
  (DeepCopier as any).DeepCopy = fastDeepCopy;
  return true;
}
