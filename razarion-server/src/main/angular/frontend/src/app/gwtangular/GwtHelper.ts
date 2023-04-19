export class GwtHelper {
  static gwtIssueNumber(integer: any): number {
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
}
