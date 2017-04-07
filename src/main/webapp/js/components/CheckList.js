// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { Check, State, TimeSeries } from '../state';
import { fetchTimeSeries } from '../actions';
import LineChart from './LineChart';

type Props = {
  timeSeries: Array<TimeSeries>,
  isLoading: boolean,
  checks: Array<Check>,
  error: ?string,
  boardName: string,
  boxTitle: string,
  fetchTimeSeries: Function
};

class CheckList extends Component {
  props: Props;

  render() {
    const { checks, isLoading, timeSeries } = this.props;
    return (
      <section className="checks">
      {
        checks.map(({ title, status, message }: Check) =>
          <div className="check" key={ title }>
            <span className={`status background ${status}`}></span>
            <div className="content">
              <h4>{title}</h4>
              {message}
              <div className="chart">
                { isLoading ? 'Loading chart data': this.generateChart(timeSeries.find(_ => _.title == title), title) }
              </div>
            </div>
          </div>
        )
      }
      </section>
    );
  }

  componentWillMount() {
    this.props.fetchTimeSeries();
  }

  generateChart(timeSeries: TimeSeries | typeof undefined, title: string) {
    if (timeSeries)
      return (
        <LineChart
          timeSeries={ timeSeries }
          label={ title }
        />
      );
    else
      return <div>Invalid time series</div>;
  }
}

const select = (state: State) => ({
  timeSeries: state.timeSeries,
  isLoading: state.isLoadingTimeSeries,
  error: state.selectedBoardView.error,
  boardName: state.currentBoard
});

const actions = (dispatch, ownProps: Props) => ({
  fetchTimeSeries: function() {
    const props = this;
    const until: number = new Date().valueOf();
    const from: number = until - 24 * 60 * 60 * 1000;
    return dispatch(fetchTimeSeries(props.boardName, ownProps.boxTitle, from, until));
  }
});

export default connect(select, actions)(CheckList);
