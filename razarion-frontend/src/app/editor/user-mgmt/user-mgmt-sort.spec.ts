import {UserMgmtComponent} from './user-mgmt.component';

/**
 * Sorting the backend's user table by a column. The table holds several lists under their headings
 * (newest players, saves, registrations), so a sort works inside each list and never moves a player
 * out from under his heading.
 */
describe('User table sort', () => {
  const heading = (name: string) => ({heading: name, rowKey: name}) as any;
  const user = (userId: string, levelNumber: number | null, systemConnectionOpened: string | null = null) =>
    ({userId, levelNumber, systemConnectionOpened, rowKey: userId}) as any;

  it('keeps the server order until a column is chosen', () => {
    const rows = [heading('A'), user('a1', 1), user('a2', 5)];
    expect(UserMgmtComponent.sortRows(rows, null, 1)).toBe(rows);
  });

  it('sorts inside each list and leaves the headings in place', () => {
    const rows = [heading('A'), user('a1', 1), user('a2', 5), heading('B'), user('b1', 3), user('b2', 9)];

    const sorted = UserMgmtComponent.sortRows(rows, 'level', -1).map(row => row.heading ?? row.userId);

    expect(sorted).toEqual(['A', 'a2', 'a1', 'B', 'b2', 'b1']);
  });

  it('puts users without a value last, whichever way round', () => {
    const rows = [heading('A'), user('never', 1, null), user('old', 1, '2026-11-01T10:00:00Z'), user('new', 1, '2026-11-23T10:00:00Z')];

    const newestFirst = UserMgmtComponent.sortRows(rows, 'lastLogin', -1).map(row => row.heading ?? row.userId);
    const oldestFirst = UserMgmtComponent.sortRows(rows, 'lastLogin', 1).map(row => row.heading ?? row.userId);

    expect(newestFirst).toEqual(['A', 'new', 'old', 'never']);
    expect(oldestFirst).toEqual(['A', 'old', 'new', 'never']);
  });
});
