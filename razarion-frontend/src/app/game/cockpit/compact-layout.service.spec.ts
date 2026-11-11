import {CompactLayoutService} from './compact-layout.service';

describe('CompactLayoutService chat dot', () => {
  let layout: CompactLayoutService;

  beforeEach(() => {
    layout = new CompactLayoutService();
    layout.compact.set(true);
  });

  it('lights while the chat is closed and goes out when it opens', () => {
    layout.markChatUnread();
    expect(layout.chatUnread()).toBeTrue();
    layout.toggle('chat');
    expect(layout.chatUnread()).toBeFalse();
  });

  it('stays out for a message that arrives while the chat is open', () => {
    layout.toggle('chat');
    layout.markChatUnread();
    expect(layout.chatUnread()).toBeFalse();
  });

  it('stays lit when another panel opens', () => {
    layout.markChatUnread();
    layout.toggle('quest');
    expect(layout.chatUnread()).toBeTrue();
  });

  it('never lights outside compact mode', () => {
    layout.compact.set(false);
    layout.markChatUnread();
    expect(layout.chatUnread()).toBeFalse();
  });
});
