export class GwtHelper {
  static gwtIssue(value: any): any {
    if (typeof value === "object") {
      for (let childValue of Object.values(value)) {
        return childValue;
      }
    }
    return value;
  }

  static gwtIssueNumber(integer: any): number {
    return <number>GwtHelper.gwtIssueNumberNull(integer); // GWT rubbish
  }

  static gwtIssueNumberNull(integer: any): number | null | undefined {
    if (integer === null || integer === undefined) {
      return integer;
    }
    if (typeof <any>integer === 'number') {
      return integer;
    }
    return <number>Object.values(integer)[0]; // GWT rubbish
  }

  static gwtIssueStringEnum(value: any, enumType: any): any {
    if (typeof value === "string") {
      return value;
    }
    if (typeof value === "number") {
      return Object.values(enumType)[value];
    }
    for (let childValue of Object.values(value)) {
      if (typeof childValue === "string") {
        return childValue;
      }
    }
    console.info(`GwtHelper: Unexpected GWT enum: '${value}' typeof: ${typeof (value)} enum ${enumType}`)
    return value;
  }

  static gwtIssueArray(array: any): any[] {
    if (array === null || array === undefined) {
      return array;
    }
    if (Array.isArray(array)) {
      return array;
    }
    return <any[]>Object.values(array)[0]; // GWT rubbish
  }

}
