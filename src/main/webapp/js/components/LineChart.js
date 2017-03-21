// @flow
import { Component } from 'react';
import { findDOMNode } from 'react-dom';
import Chart from 'chart.js';
import moment from 'moment';
import type { TimeSeries } from '../state';

type Props = {
  timeSeries: TimeSeries,
  label: string
};

class LineChart extends Component {
  props: Props;

  render() {
    return (
      <div style={ { height: '200px', width: '100%' } }>
        <canvas></canvas>
      </div>
    );
  }

  componentDidMount() {
    const { timeSeries, label } = this.props;
    const node = findDOMNode(this);
    draw(timeSeries, label, node.firstElementChild);
  }
}

const draw = (timeSeries: TimeSeries, label, node) => {
  const ctx = node.getContext('2d');
  const data = timeSeries.data.map(([value, timestamp]) => ({
    x: moment(timestamp).toDate(),
    y: value
  }));
  new Chart(ctx, {
    type: 'line',
    data: {
      datasets: [{
        label,
        data,
        borderColor: 'rgba(33, 150, 243, 0.5)',
        backgroundColor: 'rgba(0,0,0,0)'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        xAxes: [{
          type: 'time'
        }]
      }
    }
  });
};

export default LineChart;