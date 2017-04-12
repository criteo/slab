// @flow

export const Button = ({ children, onClick }: any) => (
  <button className="button" onClick={onClick}>
    {children}
  </button>
);