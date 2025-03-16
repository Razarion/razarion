export class Helpers {
    public static getCorrectedIndex(index: number, listSize: number): number {
        let correctedIndex = index % listSize;
        if (correctedIndex < 0) {
          correctedIndex += listSize;
        }
        return correctedIndex;
      }
    
    
}
  