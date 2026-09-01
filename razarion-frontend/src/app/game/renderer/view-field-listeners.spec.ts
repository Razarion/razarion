import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';

/**
 * Every camera move runs this list, and a listener that throws used to end it - so every listener
 * registered after the failing one stopped being called, on every move, for as long as the failure
 * lasted.
 *
 * That is how a broken tip task stopped the quest markers from updating on PROD on 2026-08-30: the
 * attack tip held an attacker that had scrolled out of view, threw a TypeError, and the listeners
 * behind it never ran again. The method already had a try/catch, which is exactly why it went
 * unnoticed - the app did not crash, it just quietly stopped drawing.
 *
 * Reaching into the instance rather than constructing one: a real render service needs a scene, a
 * camera and the whole Babylon engine to say something this small.
 */
describe('View field listeners', () => {
  function serviceWith(listeners: any[], onError: (error: unknown) => void) {
    const service: any = Object.create(BabylonRenderServiceAccessImpl.prototype);
    service.viewFieldListeners = listeners;
    service.viewField = {};
    service.setupViewField = () => ({
      getBottomLeft: () => ({getX: () => 0, getY: () => 0}),
      getBottomRight: () => ({getX: () => 1, getY: () => 0}),
      getTopRight: () => ({getX: () => 1, getY: () => 1}),
      getTopLeft: () => ({getX: () => 0, getY: () => 1})
    });
    service.gwtAngularService = {
      gwtAngularFacade: {inputService: {onViewFieldChanged: () => {
      }}}
    };
    spyOn(console, 'error').and.callFake((...args: any[]) => onError(args[1] ?? args[0]));
    return service;
  }

  it('keeps calling the listeners after one of them throws', () => {
    const called: string[] = [];
    let reported: unknown = null;
    const service = serviceWith([
      {onViewFieldChanged: () => called.push('before')},
      {
        onViewFieldChanged: () => {
          called.push('throws');
          throw new TypeError("Cannot read properties of null (reading 'getPosition')");
        }
      },
      {onViewFieldChanged: () => called.push('after')}
    ], error => reported = error);

    service.onViewFieldChanged();

    // 'after' is the one that used to be lost - and with it the quest marker.
    expect(called).toEqual(['before', 'throws', 'after']);
    expect(reported instanceof TypeError).toBeTrue();
  });

  it('keeps working on the next camera move', () => {
    const called: string[] = [];
    const service = serviceWith([
      {
        onViewFieldChanged: () => {
          throw new Error('still broken');
        }
      },
      {onViewFieldChanged: () => called.push('after')}
    ], () => {
    });

    service.onViewFieldChanged();
    service.onViewFieldChanged();

    // A failure that repeats must not accumulate into a permanently dead chain.
    expect(called).toEqual(['after', 'after']);
  });
});
