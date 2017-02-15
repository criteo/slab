// @flow
export * from './fetcher';

export const getCurrentBoardName = (): ?string => {
  const matched = /[/]([-_a-zA-Z0-9%]+)/.exec(document.location.pathname);
  return matched && matched[1];
};