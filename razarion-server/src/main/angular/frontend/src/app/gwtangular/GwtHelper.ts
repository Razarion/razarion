export class GwtHelper {
  static gwtIssueNumber(integer: any): number {
    if (typeof <any>integer === 'number') {
      return integer;
    }
    return <number>Object.values(integer)[0]; // GWT rubbish
  }

  static gwtIssueStringEnum(type: any): any {
    if (typeof type === "string") {
      return type;
    }
    for (let value of Object.values(type)) {
      if (typeof value === "string") {
        return value;
      }
    }
    console.info(`GwtHelper: Unexpected GWT enum: '${type}' typeof: ${typeof (type)}`)
    return type;
  }
}
