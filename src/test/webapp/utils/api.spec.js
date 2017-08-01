import { combineViewAndLayout, aggregateStatsByDay } from 'src/utils';
import moment from 'moment';

describe('api utils specs', () => {
  describe('combineLayoutAndView', () => {
    const view = {
      title: 'Board1',
      status: 'SUCCESS',
      message: 'msg',
      boxes: [{
        title: 'Box1',
        status: 'SUCCESS',
        message: 'msg1',
        checks: [{
          title: 'Check1',
          status: 'SUCCESS',
          message: 'msg11',
          label: 'l11'
        }]
      }, {
        title: 'Box2',
        status: 'WARNING',
        message: 'msg2',
        checks: [{
          title: 'Check2',
          status: 'WARNING',
          message: 'msg22',
          label: 'l22'
        }]
      }]
    };

    const links = [
      ['Box1', 'Box2']
    ];

    const layout = {
      columns: [
        {
          percentage: 50,
          rows: [{
            title: 'Zone 1',
            percentage: 100,
            boxes: [{
              title: 'Box1',
              description: 'desc1',
              labelLimit: 2
            }]
          }]
        },
        {
          percentage: 50,
          rows: [{
            title: 'Zone 2',
            percentage: 100,
            boxes: [{
              title: 'Box2',
              description: 'desc2',
              labelLimit: 0
            }]
          }]
        }
      ]
    };

    it('combines board layout and view data, returns an object representing the view', () => {
      const result = combineViewAndLayout(view, layout, links);
      expect(result).to.deep.equal({
        title: 'Board1',
        status: 'SUCCESS',
        message: 'msg',
        columns: [
          {
            percentage: 50,
            rows: [{
              title: 'Zone 1',
              percentage: 100,
              boxes: [{
                title: 'Box1',
                description: 'desc1',
                status: 'SUCCESS',
                message: 'msg1',
                labelLimit: 2,
                checks: [{
                  title: 'Check1',
                  status: 'SUCCESS',
                  message: 'msg11',
                  label: 'l11'
                }]
              }]
            }]
          },
          {
            percentage: 50,
            rows: [{
              title: 'Zone 2',
              percentage: 100,
              boxes: [{
                title: 'Box2',
                status: 'WARNING',
                description: 'desc2',
                message: 'msg2',
                labelLimit: 0,
                checks: [{
                  title: 'Check2',
                  status: 'WARNING',
                  message: 'msg22',
                  label: 'l22'
                }]
              }]
            }]
          }
        ],
        links
      });
    });
  });

  describe('aggregateStatsByDay', () => {
    it('takes hourly stats and aggregate them into daily buckets', () => {
      const stats = {
        [moment('2000-01-01 00:00').valueOf()]: {
          successes: 1,
          warnings: 1,
          errors: 1,
          unknown: 0,
          total: 3
        },
        [moment('2000-01-01 23:00').valueOf()]: {
          successes: 1,
          warnings: 1,
          errors: 1,
          unknown: 0,
          total: 3
        },
        [moment('2000-01-02 00:00').valueOf()]: {
          successes: 3,
          warnings: 2,
          errors: 1,
          unknown: 0,
          total: 6
        },
        [moment('2000-01-02 23:59').valueOf()]: {
          successes: 0,
          warnings: 0,
          errors: 0,
          unknown: 3,
          total: 3
        },
      };
      const res = aggregateStatsByDay(stats);
      expect(res).to.deep.equal(
        {
          [moment('2000-01-01 00:00').valueOf()]: {
            successes: 2,
            warnings: 2,
            errors: 2,
            unknown: 0,
            total: 6
          },
          [moment('2000-01-02 00:00').valueOf()]: {
            successes: 3,
            warnings: 2,
            errors: 1,
            unknown: 3,
            total: 9
          },
        }
      );
    });
  });
});