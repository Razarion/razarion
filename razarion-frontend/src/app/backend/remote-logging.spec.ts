import {RemoteLogging} from './remote-logging';

describe('RemoteLogging', () => {
  afterEach(() => RemoteLogging.resume());

  it('knows the director page', () => {
    expect(RemoteLogging.isDirectorPage('/game/director')).toBeTrue();
    expect(RemoteLogging.isDirectorPage('/game/director/')).toBeTrue();
    expect(RemoteLogging.isDirectorPage('/game')).toBeFalse();
    expect(RemoteLogging.isDirectorPage('/game/')).toBeFalse();
    expect(RemoteLogging.isDirectorPage('/game/backend')).toBeFalse();
  });

  it('forwards warnings from a player page', () => {
    spyOn(RemoteLogging, 'isDirectorPage').and.returnValue(false);
    expect(RemoteLogging.allow('warn')).toBeTrue();
  });

  it('forwards only errors from the director page', () => {
    spyOn(RemoteLogging, 'isDirectorPage').and.returnValue(true);
    expect(RemoteLogging.allow('warn')).toBeFalse();
    expect(RemoteLogging.allow('error')).toBeTrue();
  });
});
