// @flow
import { Component } from 'react';
import { findDOMNode, render } from 'react-dom';
import { connect } from 'react-redux';
import vis from 'vis';
import moment from 'moment';
import type { State } from '../state';
import { navigateToSnapshot, navigateToLiveBoard } from '../actions';
import Controller from './TimelineController';

type Props = {
  history: any,
  error: ?string,
  isLoading: boolean,
  isLiveMode: boolean,
  navigateToSnapshot: (timestamp: number) => void,
  navigateToLiveBoard: () => void,
  selectedDate: ?string,
  selectedTimestamp: ?number
};

class Timeline extends Component {
  props: Props;
  state: {
    hasFocus: boolean
  };

  timeline: any;
  dataset: any;
  static DATE_FORMAT = 'YYYY-MM-DD HH:mm';

  constructor(props: Props) {
    super(props);
    this.timeline = null;
    this.dataset = null;
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
        {
          history && (
            <div id="container">
              <div id="message"></div>
            </div>
          )
        }
      </div>
    );
  }

  componentDidMount() {
    this.updateAndRender();
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

    if (!isLoading) {
      this.updateAndRender();
    }
  }

  updateAndRender() {
    this.updateDataset();
    this.renderTimeline();
  }

  updateDataset() {
    this.dataset = Object.entries(this.props.history)
        .filter(
          ([_, status]: [string, any]) =>
            status === 'ERROR' || status === 'WARNING'
        )
        .map(([ts, status]: [string, any], i) => {
          const date = moment(parseInt(ts));
          return {
            date,
            id: i,
            content: '',
            start: date.format(Timeline.DATE_FORMAT),
            title: date.format(Timeline.DATE_FORMAT),
            className: `${status} background`
          };
        })
        .sort((a, b) => a.start.localeCompare(b.start));
  }

  renderTimeline() {
    const node = findDOMNode(this);
    const container = node.querySelector('#container');
    const { history, selectedTimestamp, selectedDate } = this.props;
    // if selectedDate is not defined, defaults to last 24 hours
    const start = selectedDate ? moment(selectedDate) : moment().subtract(24, 'hour');
    const end = selectedDate ? moment(selectedDate).add(24, 'hour') : moment();
    if (!this.timeline) {
      const timeline = new vis.Timeline(container, this.dataset, {
        height: 75,
        type: 'point',
        stack: false,
        zoomMin: 60 * 1000,
        start,
        end
      });
      timeline.on('select', ({ items }) => {
        if (items.length > 0) {
          const entry = this.dataset.find(_ => _.id === items[0]);
          const timestamp = entry && entry.date.valueOf();
          timestamp && this.props.navigateToSnapshot(timestamp);
        } else {
          this.props.navigateToLiveBoard();
        }
      });
      this.timeline = timeline;
    } else {
      this.timeline.setWindow(start.valueOf(), end.valueOf());
      this.timeline.setItems(this.dataset);
    }

    if (this.dataset && this.dataset.length == 0) {
      render(
        <div className="no-data">
          {
            Object.keys(history).length > 0 ?
            <div>No warnings or errors in this period</div> :
            <div>No data available</div>
          }
        </div>,
        container.querySelector('#message')
      );
    } else {
      render(
        <div></div>,
        container.querySelector('#message')
      );
    }
    if (selectedTimestamp) {
      const id = this.dataset.find(_ => _.date.valueOf() === selectedTimestamp);
      id && this.timeline.setSelection([id.id]);
    }
  }
}

const select = (state: State) => ({
  history: state.history.data,
  selectedDate: state.history.date,
  error: state.history.error,
  isLoading: state.history.isLoading,
  isLiveMode: state.isLiveMode,
  selectedTimestamp: state.selectedTimestamp,
  board: state.currentBoard
});

const actions = (dispatch) => ({
  navigateToSnapshot: function(timestamp) {
    const props = this;
    dispatch(navigateToSnapshot(props.board, timestamp));
  },
  navigateToLiveBoard: function() {
    const props = this;
    dispatch(navigateToLiveBoard(props.board));
  }
});

export default connect(select, actions)(Timeline);
