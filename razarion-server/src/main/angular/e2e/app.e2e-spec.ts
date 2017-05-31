import { BackendPage } from './app.po';

describe('backend App', () => {
  let page: BackendPage;

  beforeEach(() => {
    page = new BackendPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
