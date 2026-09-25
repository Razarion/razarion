// Thrown by a format when there is nothing worth making right now. Not a failure: produce.mjs
// prints the reason and exits cleanly, and an unattended run moves on to the next format.
export class NotReady extends Error {}
