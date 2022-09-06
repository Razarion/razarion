export class GwtHelper {
  static gwtIssueNumber(integer: any): number {
    if (typeof <any>integer === 'number') {
      return integer;
    }
    return <number>Object.values(integer)[0]; // GWT rubbish
  }
}
