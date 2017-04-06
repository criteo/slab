// @flow
import injectSheet from 'react-jss';

const styles = {
  button: {
    background: 'transparent',
    color: 'white',
    border: '0',
    margin: '0 .1em',
    'cursor': 'pointer',
    'font-size': '12px',
    'border-top': '2px solid transparent',
    'border-bottom': '2px solid transparent',
    '&:hover': {
      'color': '#A9A9A9',
      'border-bottom': '2px solid #4CAF50',
      background: 'rgba(255,255,255,.1)'
    },
    '&:active': {
      'color': 'white'
    },
    '&:focus': {
      'outline': '0'
    }
  }
};

const Comp = ({ classes, children, onClick }) => (
  <button
    className={classes.button}
    onClick={onClick}
  >
    {children}
  </button>
);

export const Button = injectSheet(styles)(Comp);