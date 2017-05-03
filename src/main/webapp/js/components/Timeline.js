// @flow
import { Component } from 'react';
import { findDOMNode, render } from 'react-dom';
import { connect } from 'react-redux';
import vis from 'vis';
import moment from 'moment';
import type { State } from '../state';
import { switchBoardView } from '../actions';
import Controller from './TimelineController';

type Props = {
  history: any,
  error: ?string,
  isLoading: boolean,
  isLiveMode: boolean,
  switchBoardView: (isLiveMode: boolean, timestamp?: number) => void,
  selectedDate: ?string
};

class Timeline extends Component {
  props: Props;
  state: {
    hasFocus: boolean
  };

  timeline: any;
  static DATE_FORMAT = 'YYYY-MM-DD HH:mm';

  constructor(props: Props) {
    super(props);
    this.timeline = null;
    this.state = {
      hasFocus: false
    };
  }

  render() {
    const { history } = this.props;
    return (
      <div
        className="timeline"
        onMouseEnter={() => this.setState({ hasFocus: true })}
        onMouseLeave={() => this.setState({ hasFocus: false })}
      >
        <Controller />
        {history && <div id="container" />}
      </div>
    );
  }

  componentDidMount() {
    this.renderTimeline();
  }

  shouldComponentUpdate(nextProps, nextState) {
    // No update: go back to a past timepoint
    if (this.props.isLiveMode === true && nextProps.isLiveMode === false) {
      return false;
    }
    // No update: focus state changes
    if (this.state.hasFocus !== nextState.hasFocus)
      return false;
    return true;
  }

  componentDidUpdate(prevProps) {
    const { isLiveMode, isLoading } = this.props;
    const { hasFocus } = this.state;
    // Switch from past to live mode
    if (prevProps.isLiveMode === false && isLiveMode === true) {
      this.timeline && this.timeline.setSelection([]);
      return;
    }
    // Do nothing if the user is focusing on the timeline
    if (hasFocus && (prevProps.isLoading === isLoading))
      return;

    this.renderTimeline();
  }

  renderTimeline() {
    const node = findDOMNode(this);
    const container = node.querySelector('#container');
    if (container) {
      container.innerHTML = '';
      const { history } = this.props;
      const dataset = Object.entries(history)
        .filter(
          ([_, view]: [string, any]) =>
            view.status === 'ERROR' || view.status === 'WARNING'
        )
        .map(([ts, view]: [string, any], i) => {
          const date = moment(parseInt(ts));
          return {
            date,
            id: i,
            content: '',
            start: date.format(Timeline.DATE_FORMAT),
            title: date.format(Timeline.DATE_FORMAT),
            className: `${view.status} background`
          };
        })
        .sort((a, b) => a.start.localeCompare(b.start));
      if (dataset.length > 0) {
        // if selectedDate is not defined, defaults to last 24 hours
        const { selectedDate } = this.props;
        const min = selectedDate ? moment(selectedDate) : moment().subtract(24, 'hour');
        const max = selectedDate ? moment(selectedDate).add(24, 'hour') : moment();
        const timeline = new vis.Timeline(container, dataset, {
          height: 75,
          type: 'point',
          stack: false,
          zoomMin: 60 * 1000,
          min,
          max,
          start: min,
          end: max
        });
        timeline.on('select', ({ items }) => {
          if (items.length > 0) {
            const entry = dataset.find(_ => _.id === items[0]);
            const timestamp = entry && entry.date.valueOf();
            this.props.switchBoardView(false, timestamp);
          } else {
            this.props.switchBoardView(true);
          }
        });
        this.timeline = timeline;
      } else {
        render(
          <div className="info">
            {
              Object.keys(history).length > 0 ?
              <div>No warnings or errors in this period</div> :
              <div>No data available</div>
            }
          </div>,
          container
        );
      }
    }
  }
}

const select = (state: State) => ({
  history: state.history.data,
  selectedDate: state.history.date,
  error: state.history.error,
  isLoading: state.history.isLoading,
  isLiveMode: state.isLiveMode
});

const actions = (dispatch) => ({
  switchBoardView: (isLiveMode, timestamp = 0) =>
    dispatch(switchBoardView(isLiveMode, timestamp))
});

export default connect(select, actions)(Timeline);
