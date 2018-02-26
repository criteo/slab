// @flow
import { PureComponent } from 'react';
import type { Status } from '../state';
import { statusToColor } from '../utils';

type Props = {
  status: Status
};

const Radius = 32;

class StatusFavicon extends PureComponent {
  props: Props;

  canvas = null;

  render() {
    return null;
  }

  componentDidUpdate() {
    this.update();
  }

  componentDidMount() {
    this.update();
  }

  componentWillUnmount() {
    const favicon = document.querySelector('#favicon');
    if (favicon instanceof HTMLLinkElement) {
      favicon.href = '';
    }
  }

  update() {
    let { canvas, props: { status } } = this;
    if (canvas === null) {
      this.canvas = canvas = document.createElement('canvas');
      canvas.width = 2 * Radius;
      canvas.height = 2 * Radius;
    }
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.beginPath();
      ctx.arc(Radius, Radius, Radius / 2, 0, 2 * Math.PI);
      ctx.fillStyle = statusToColor(status);
      ctx.fill();
    }
    const favicon = document.querySelector('#favicon');
    if (favicon instanceof HTMLLinkElement) {
      favicon.href = canvas.toDataURL('image/x-icon');
    }
  }
}

export default StatusFavicon;